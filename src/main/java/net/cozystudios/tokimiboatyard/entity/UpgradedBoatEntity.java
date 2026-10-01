package net.cozystudios.tokimiboatyard.entity;

import net.cozystudios.tokimiboatyard.registry.ModEntities;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.cozystudios.tokimiboatyard.screen.UpgradedBoatScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class UpgradedBoatEntity extends BoatEntity implements RideableInventory, VehicleInventory, ExtendedScreenHandlerFactory {
    private static final TrackedData<Integer> TIER =
        DataTracker.registerData(UpgradedBoatEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Integer> EXTRA_SEATS =
        DataTracker.registerData(UpgradedBoatEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<NbtCompound> SEATS =
        DataTracker.registerData(UpgradedBoatEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    public static final float SEGMENT_SPACING = 1.75F;

    public static final int MAX_EXTRA_SEATS = 6;

    private DefaultedList<ItemStack> inventory =
        DefaultedList.ofSize(BoatTier.COPPER.slots(), ItemStack.EMPTY);

    private @Nullable Identifier lootTableId;
    private long lootTableSeed;

    private int pendingSeat = -1;

    private float lastSafeYaw;
    private boolean lastSafeInitialized = false;

    public UpgradedBoatEntity(EntityType<? extends BoatEntity> type, World world) {
        super(type, world);
    }

    public UpgradedBoatEntity(World world, double x, double y, double z) {
        this(ModEntities.UPGRADED_BOAT, world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TIER, BoatTier.COPPER.ordinal());
        this.dataTracker.startTracking(EXTRA_SEATS, 0);
        this.dataTracker.startTracking(SEATS, new NbtCompound());
    }


    public BoatTier getTier() {
        return BoatTier.fromId(this.dataTracker.get(TIER));
    }

    public void setTier(BoatTier tier) {
        BoatTier previous = getTier();
        this.dataTracker.set(TIER, tier.ordinal());
        this.resizeInventoryForTier();
        if (!this.getWorld().isClient && previous != tier) {
        }
    }


    public int getExtraSeats() {
        return this.dataTracker.get(EXTRA_SEATS);
    }

    public void setExtraSeats(int n) {
        int clamped = Math.max(0, Math.min(MAX_EXTRA_SEATS, n));
        int previous = getExtraSeats();
        if (clamped == previous) return;
        this.dataTracker.set(EXTRA_SEATS, clamped);
        if (!this.getWorld().isClient) {
            respawnParts();
        }
    }

    public int getTotalSeats() {
        return 2 + getExtraSeats();
    }

    public int getExtraParts() {
        return (getExtraSeats() + 1) / 2;
    }


    private void resizeInventoryForTier() {
        int newSize = getTier().slots();
        if (this.inventory.size() == newSize) return;
        DefaultedList<ItemStack> old = this.inventory;
        DefaultedList<ItemStack> fresh = DefaultedList.ofSize(newSize, ItemStack.EMPTY);
        int copyCount = Math.min(newSize, old.size());
        for (int i = 0; i < copyCount; i++) fresh.set(i, old.get(i));
        this.inventory = fresh;
        if (!this.getWorld().isClient && old.size() > newSize) {
            for (int i = newSize; i < old.size(); i++) {
                if (!old.get(i).isEmpty()) {
                    ItemScatterer.spawn(getWorld(), getX(), getY() + 0.5, getZ(), old.get(i));
                }
            }
        }
    }


    public void createBoatParts() {
        if (this.getWorld().isClient) return;
        int n = getExtraParts();
        for (int i = 1; i <= n; i++) {
            UpgradedBoatPart part = new UpgradedBoatPart(ModEntities.UPGRADED_BOAT_PART, this.getWorld());
            part.setIndex(i);
            part.setOffsetZ(-i * SEGMENT_SPACING);
            part.setPosition(this.getX(), this.getY(), this.getZ());
            this.getWorld().spawnEntity(part);
            part.startRiding(this, true);
        }
    }

    private void respawnParts() {
        for (Entity e : new ArrayList<>(this.getPassengerList())) {
            if (e instanceof UpgradedBoatPart) {
                e.stopRiding();
                e.discard();
            }
        }
        createBoatParts();
    }

    private long currentPartCount() {
        return this.getPassengerList().stream().filter(e -> e instanceof UpgradedBoatPart).count();
    }


    private NbtCompound seatsNbt() { return this.dataTracker.get(SEATS); }

    public int getSeatOf(UUID id) {
        NbtCompound tag = seatsNbt();
        String key = id.toString();
        return tag.contains(key) ? tag.getInt(key) : -1;
    }

    private boolean hasSeat(UUID id) { return seatsNbt().contains(id.toString()); }

    private @Nullable UUID occupantOfSeat(int seat) {
        NbtCompound tag = seatsNbt();
        for (String key : tag.getKeys()) {
            if (tag.getInt(key) == seat) {
                try { return UUID.fromString(key); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    private boolean isSeatFree(int seat) { return occupantOfSeat(seat) == null; }

    private void putSeat(UUID id, int seat) {
        NbtCompound copy = seatsNbt().copy();
        copy.putInt(id.toString(), seat);
        this.dataTracker.set(SEATS, copy);
    }

    private void removeSeatEntry(UUID id) {
        NbtCompound tag = seatsNbt();
        String key = id.toString();
        if (!tag.contains(key)) return;
        NbtCompound copy = tag.copy();
        copy.remove(key);
        this.dataTracker.set(SEATS, copy);
    }

    public int firstFreeSeatInSegment(int segmentIndex) {
        int base = segmentIndex * 2;
        for (int i = 0; i < 2; i++) {
            int s = base + i;
            if (s < getTotalSeats() && isSeatFree(s)) return s;
        }
        return -1;
    }

    public boolean hasPassengerInSegment(int segmentIndex) {
        int base = segmentIndex * 2;
        int max = getTotalSeats();
        for (Entity p : this.getPassengerList()) {
            if (p instanceof UpgradedBoatPart) continue;
            int seat = getSeatOf(p.getUuid());
            if (seat >= base && seat < base + 2 && seat < max) return true;
        }
        return false;
    }

    public int firstFreeSeat() {
        for (int s = 0; s < getTotalSeats(); s++) if (isSeatFree(s)) return s;
        return -1;
    }

    public void setPendingSeat(int seat) { this.pendingSeat = seat; }

    private void pruneStaleSeats() {
        NbtCompound tag = seatsNbt();
        if (tag.isEmpty()) return;
        Set<String> aboard = new HashSet<>();
        for (Entity p : getPassengerList()) {
            if (!(p instanceof UpgradedBoatPart)) aboard.add(p.getUuid().toString());
        }
        NbtCompound cleaned = new NbtCompound();
        boolean changed = false;
        for (String key : tag.getKeys()) {
            if (aboard.contains(key)) cleaned.putInt(key, tag.getInt(key));
            else changed = true;
        }
        int max = getTotalSeats();
        NbtCompound cleaned2 = new NbtCompound();
        for (String key : cleaned.getKeys()) {
            int seat = cleaned.getInt(key);
            if (seat < max) cleaned2.putInt(key, seat);
            else changed = true;
        }
        if (changed) this.dataTracker.set(SEATS, cleaned2);
    }


    @Override
    protected int getMaxPassengers() {
        return getTotalSeats() + getExtraParts();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        if (passenger instanceof UpgradedBoatPart) return true;
        long regulars = this.getPassengerList().stream()
            .filter(e -> !(e instanceof UpgradedBoatPart)).count();
        return regulars < getTotalSeats() && !this.isSubmergedIn(FluidTags.WATER);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof UpgradedBoatPart) return;
        if (this.getWorld().isClient) return;
        UUID id = passenger.getUuid();
        if (hasSeat(id)) return;
        int seat = -1;
        if (this.pendingSeat >= 0 && isSeatFree(this.pendingSeat)) seat = this.pendingSeat;
        if (seat < 0) seat = firstFreeSeat();
        if (seat >= 0) putSeat(id, seat);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger instanceof UpgradedBoatPart) return;
        if (this.getWorld().isClient) return;
        removeSeatEntry(passenger.getUuid());
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater updater) {
        if (!this.hasPassenger(passenger)) return;

        if (passenger instanceof UpgradedBoatPart part) {
            Vec3d local = new Vec3d(0.0, 0.0, part.getOffsetZ())
                .rotateY(-this.getYaw() * ((float) Math.PI / 180f));
            updater.accept(passenger,
                this.getX() + local.x,
                this.getY(),
                this.getZ() + local.z);
            passenger.setYaw(this.getYaw());
            passenger.setHeadYaw(this.getYaw());
            return;
        }

        int seatIdx = getSeatOf(passenger.getUuid());
        if (seatIdx < 0) seatIdx = regularPassengerIndex(passenger);

        int segment = seatIdx / 2;
        int seatInSeg = seatIdx % 2;
        float zInSeg = seatInSeg == 0 ? 0.2f : -0.6f;
        float localZ = -segment * SEGMENT_SPACING + zInSeg;

        Vec3d local = new Vec3d(0.0, this.getMountedHeightOffset() + passenger.getHeightOffset(), localZ)
            .rotateY(-this.getYaw() * ((float) Math.PI / 180f));
        updater.accept(passenger,
            this.getX() + local.x,
            this.getY() + local.y,
            this.getZ() + local.z);
        clampPassengerYawInline(passenger);
        if (passenger instanceof net.minecraft.entity.passive.AnimalEntity animal
            && this.regularPassengerCount() > 1) {
            int rotation = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setBodyYaw(animal.bodyYaw + rotation);
            passenger.setHeadYaw(passenger.getHeadYaw() + rotation);
        }
    }

    private int regularPassengerIndex(Entity passenger) {
        int idx = 0;
        for (Entity p : this.getPassengerList()) {
            if (p instanceof UpgradedBoatPart) continue;
            if (p == passenger) return idx;
            idx++;
        }
        return 0;
    }

    private int regularPassengerCount() {
        int n = 0;
        for (Entity p : this.getPassengerList()) if (!(p instanceof UpgradedBoatPart)) n++;
        return n;
    }

    private void clampPassengerYawInline(Entity passenger) {
        passenger.setBodyYaw(this.getYaw());
        float delta = MathHelper.wrapDegrees(passenger.getYaw() - this.getYaw());
        float clamped = MathHelper.clamp(delta, -105.0F, 105.0F);
        passenger.prevYaw += clamped - delta;
        passenger.setYaw(passenger.getYaw() + clamped - delta);
        passenger.setHeadYaw(passenger.getYaw());
    }


    @Override
    public void tick() {
        if (getTier().fireproof() && !this.getWorld().isClient) {
            this.setFireTicks(0);
        }
        super.tick();
        if (getTier().fireproof()) {
            applyLavaBuoyancy();
        }

        if (!this.getWorld().isClient && this.isAlive() && this.age > 20 && this.age % 20 == 0) {
            int expected = getExtraParts();
            if (currentPartCount() < expected) {
                respawnParts();
            }
            pruneStaleSeats();
        }

        if (!this.getWorld().isClient) {
            autoMountAtSegment(0, this.getBoundingBox());
        }

        if (!this.getWorld().isClient && getExtraParts() > 0 && this.isAlive()) {
            validateNoClipOrRollback();
        }
    }

    private void validateNoClipOrRollback() {
        this.setBoundingBox(calculateBoundingBox());
        boolean overlapsTerrain =
            this.getWorld().getBlockCollisions(this, this.getBoundingBox()).iterator().hasNext();

        if (!overlapsTerrain) {
            this.lastSafeYaw = this.getYaw();
            this.lastSafeInitialized = true;
            return;
        }

        if (!this.lastSafeInitialized) return;

        super.setYaw(this.lastSafeYaw);
        this.setBoundingBox(calculateBoundingBox());
        for (Entity passenger : this.getPassengerList()) {
            if (passenger instanceof UpgradedBoatPart) {
                this.updatePassengerPosition(passenger, Entity::setPos);
            }
        }
    }

    public void autoMountAtSegment(int segmentIndex, Box searchBox) {
        Box expanded = searchBox.expand(0.2, -0.01, 0.2);
        List<Entity> nearby = this.getWorld().getOtherEntities(this, expanded,
            e -> !e.isSpectator() && e.canHit());
        for (Entity e : nearby) {
            if (e.hasVehicle()) continue;
            if (e instanceof UpgradedBoatPart) continue;
            if (e instanceof PlayerEntity) continue;
            if (!(e instanceof LivingEntity)) continue;
            if (!(e instanceof PassiveEntity)) continue;
            if (e instanceof WaterCreatureEntity) continue;
            if (e.getWidth() >= this.getWidth()) continue;
            if (!canAddPassenger(e)) continue;

            int seat = firstFreeSeatInSegment(segmentIndex);
            if (seat < 0) seat = firstFreeSeat();
            if (seat < 0) continue;
            setPendingSeat(seat);
            e.startRiding(this);
            setPendingSeat(-1);
        }
    }

    private void applyLavaBuoyancy() {
        BlockPos here = getBlockPos();
        BlockPos below = here.down();
        boolean inLava = this.getWorld().getFluidState(here).isIn(FluidTags.LAVA);
        boolean overLava = this.getWorld().getFluidState(below).isIn(FluidTags.LAVA);
        if (!inLava && !overLava) return;
        Vec3d v = this.getVelocity();
        double vy = v.y;
        if (inLava) {
            vy = Math.max(vy + 0.04, 0.03);
            vy = Math.min(vy, 0.12);
        } else if (vy < 0) {
            vy = 0;
        }
        this.setVelocity(v.x * 0.9, vy, v.z * 0.9);
        this.fallDistance = 0;
    }

    @Override
    public boolean isFireImmune() {
        return getTier().fireproof() || super.isFireImmune();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (getTier().fireproof() && source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return super.damage(source, amount);
    }


    @Override
    public DefaultedList<ItemStack> getInventory() { return this.inventory; }

    @Override
    public void resetInventory() {
        this.inventory = DefaultedList.ofSize(getTier().slots(), ItemStack.EMPTY);
    }

    @Override
    public @Nullable Identifier getLootTableId() { return this.lootTableId; }
    @Override
    public void setLootTableId(@Nullable Identifier id) { this.lootTableId = id; }
    @Override
    public long getLootTableSeed() { return this.lootTableSeed; }
    @Override
    public void setLootTableSeed(long seed) { this.lootTableSeed = seed; }
    @Override
    public int size() { return this.inventory.size(); }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return !this.isRemoved() && this.squaredDistanceTo(player) <= 64.0;
    }

    @Override
    public void markDirty() {}

    @Override
    public void clear() { this.inventory.clear(); }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStack(int slot) { return this.inventory.get(slot); }

    @Override
    public ItemStack removeStack(int slot, int amount) { return Inventories.splitStack(this.inventory, slot, amount); }

    @Override
    public ItemStack removeStack(int slot) { return Inventories.removeStack(this.inventory, slot); }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
    }


    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        return interactSegment(player, hand, pickedSegmentForPlayer(player));
    }

    private int pickedSegmentForPlayer(PlayerEntity player) {
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d worldRayEnd = eyePos.add(lookVec.multiply(15.0));

        float yawRad = getYaw() * ((float) Math.PI / 180F);
        Vec3d headPos = new Vec3d(this.getX(), this.getY(), this.getZ());
        Vec3d localEye = eyePos.subtract(headPos).rotateY(yawRad);
        Vec3d localEnd = worldRayEnd.subtract(headPos).rotateY(yawRad);

        int bestSegment = 0;
        double bestDistSq = Double.MAX_VALUE;
        boolean anyHit = false;

        int totalSegments = getExtraParts() + 1;
        double halfSeg = SEGMENT_SPACING / 2.0;
        for (int i = 0; i < totalSegments; i++) {
            double centerZ = -i * SEGMENT_SPACING;
            Box localBox = new Box(
                -0.75, 0.0, centerZ - halfSeg,
                 0.75, 0.5625, centerZ + halfSeg
            );
            Optional<Vec3d> hit = localBox.raycast(localEye, localEnd);
            if (hit.isEmpty()) continue;
            double d = localEye.squaredDistanceTo(hit.get());
            if (!anyHit || d < bestDistSq) {
                bestDistSq = d;
                bestSegment = i;
                anyHit = true;
            }
        }
        return bestSegment;
    }

    public ActionResult interactSegment(PlayerEntity player, Hand hand, int segmentIndex) {
        if (this.canAddPassenger(player) && !player.shouldCancelInteraction()) {
            int seat = firstFreeSeatInSegment(segmentIndex);
            if (seat < 0) seat = firstFreeSeat();
            if (seat >= 0) {
                if (this.getWorld().isClient) return ActionResult.SUCCESS;
                this.pendingSeat = seat;
                boolean ok = player.startRiding(this);
                this.pendingSeat = -1;
                return ok ? ActionResult.CONSUME : ActionResult.PASS;
            }
        }
        ActionResult result = this.open(player);
        if (result.isAccepted() && !this.getWorld().isClient) {
            this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinBrain.onGuardedBlockInteracted(player, true);
        }
        return result;
    }

    @Override
    public void openInventory(PlayerEntity player) {
        ActionResult result = this.open(player);
        if (result.isAccepted() && !this.getWorld().isClient) {
            this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinBrain.onGuardedBlockInteracted(player, true);
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Vec3d dismountOffset = BoatEntity.getPassengerDismountOffset(
            this.getWidth() * MathHelper.SQUARE_ROOT_OF_TWO,
            passenger.getWidth(),
            passenger.getYaw());
        double dx = passenger.getX() + dismountOffset.x;
        double dz = passenger.getZ() + dismountOffset.z;
        BlockPos posTop = BlockPos.ofFloored(dx, this.getBoundingBox().maxY, dz);
        BlockPos posBelow = posTop.down();
        if (!this.getWorld().isWater(posBelow)) {
            List<Vec3d> candidates = new ArrayList<>();
            double topH = this.getWorld().getDismountHeight(posTop);
            if (Dismounting.canDismountInBlock(topH)) {
                candidates.add(new Vec3d(dx, posTop.getY() + topH, dz));
            }
            double belowH = this.getWorld().getDismountHeight(posBelow);
            if (Dismounting.canDismountInBlock(belowH)) {
                candidates.add(new Vec3d(dx, posBelow.getY() + belowH, dz));
            }
            for (EntityPose pose : passenger.getPoses()) {
                for (Vec3d candidate : candidates) {
                    if (Dismounting.canPlaceEntityAt(this.getWorld(), candidate, passenger, pose)) {
                        passenger.setPose(pose);
                        return candidate;
                    }
                }
            }
        }
        return new Vec3d(passenger.getX(), this.getBoundingBox().maxY, passenger.getZ());
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.getWorld().isClient && reason.shouldDestroy()) {
            ItemScatterer.spawn(this.getWorld(), this, this);
        }
        if (!this.getWorld().isClient) {
            for (Entity e : new ArrayList<>(this.getPassengerList())) {
                if (e instanceof UpgradedBoatPart) {
                    e.stopRiding();
                    e.discard();
                }
            }
        }
        super.remove(reason);
    }

    @Override
    protected void dropItems(DamageSource source) {
        if (!this.getWorld().isClient) {
            this.dropStack(createItemStackWithNbt());
        }
    }


    private static final double PART_HALF_WIDTH = 0.5;

    @Override
    protected Box calculateBoundingBox() {
        Box head = super.calculateBoundingBox();
        int extraParts = getExtraParts();
        if (extraParts == 0) return head;

        double minX = head.minX, maxX = head.maxX;
        double minY = head.minY, maxY = head.maxY;
        double minZ = head.minZ, maxZ = head.maxZ;

        float rad = getYaw() * ((float) Math.PI / 180F);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        double headX = this.getX();
        double headZ = this.getZ();

        for (int i = 1; i <= extraParts; i++) {
            double partCenterX = headX + sin * i * SEGMENT_SPACING;
            double partCenterZ = headZ - cos * i * SEGMENT_SPACING;
            minX = Math.min(minX, partCenterX - PART_HALF_WIDTH);
            maxX = Math.max(maxX, partCenterX + PART_HALF_WIDTH);
            minZ = Math.min(minZ, partCenterZ - PART_HALF_WIDTH);
            maxZ = Math.max(maxZ, partCenterZ + PART_HALF_WIDTH);
        }
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        this.setBoundingBox(calculateBoundingBox());
        super.move(type, movement);
    }

    @Override
    public void setYaw(float yaw) {
        if (this.getWorld() == null || getExtraParts() == 0) {
            super.setYaw(yaw);
            return;
        }
        if (yaw == this.getYaw()) {
            return;
        }
        float oldYaw = this.getYaw();
        Box oldBox = calculateBoundingBox();
        super.setYaw(yaw);
        Box newBox = calculateBoundingBox();
        boolean newOverlaps =
            this.getWorld().getBlockCollisions(this, newBox).iterator().hasNext();
        if (!newOverlaps) return;
        boolean oldOverlaps =
            this.getWorld().getBlockCollisions(this, oldBox).iterator().hasNext();
        if (!oldOverlaps) {
            super.setYaw(oldYaw);
        }
    }

    public ItemStack createItemStackWithNbt() {
        ItemStack stack = new ItemStack(this.asItem());
        int extras = getExtraSeats();
        if (extras > 0) {
            stack.getOrCreateNbt().putInt("ExtraSeats", extras);
        }
        return stack;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return createItemStackWithNbt();
    }

    @Override
    public Item asItem() { return ModItems.get(getTier(), this.getVariant()); }


    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Tier", getTier().ordinal());
        nbt.putInt("ExtraSeats", getExtraSeats());
        nbt.put("Seats", seatsNbt().copy());
        this.writeInventoryToNbt(nbt);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Tier", NbtElement.INT_TYPE)) {
            BoatTier tier = BoatTier.fromId(nbt.getInt("Tier"));
            this.dataTracker.set(TIER, tier.ordinal());
            this.inventory = DefaultedList.ofSize(tier.slots(), ItemStack.EMPTY);
        }
        if (nbt.contains("ExtraSeats", NbtElement.INT_TYPE)) {
            int val = Math.max(0, Math.min(MAX_EXTRA_SEATS, nbt.getInt("ExtraSeats")));
            this.dataTracker.set(EXTRA_SEATS, val);
        }
        if (nbt.contains("Seats", NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(SEATS, nbt.getCompound("Seats").copy());
        }
        this.readInventoryFromNbt(nbt);
    }


    @Override
    public Text getDisplayName() {
        return Text.translatable("entity.tokimiboatyard.upgraded_boat." + getTier().id());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if (this.lootTableId != null && player.isSpectator()) return null;
        this.generateInventoryLoot(player);
        return new UpgradedBoatScreenHandler(syncId, inv, this, getTier().rows());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeByte(getTier().rows());
    }
}
