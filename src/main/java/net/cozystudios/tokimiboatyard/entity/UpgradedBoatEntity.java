package net.cozystudios.tokimiboatyard.entity;

import net.cozystudios.tokimiboatyard.registry.ModEntities;
import net.cozystudios.tokimiboatyard.registry.ModItems;
import net.cozystudios.tokimiboatyard.item.UpgradedBoatItem;
import net.cozystudios.tokimiboatyard.util.ModIds;
import net.cozystudios.tokimiboatyard.screen.UpgradedBoatScreenHandler;
//? if fabric
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
//? if neoforge {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.client.extensions.IMenuProviderExtension;
import net.neoforged.neoforge.common.NeoForgeMod;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
//? if >=1.21.1 {
/*import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
*///? } else {
import net.minecraft.network.FriendlyByteBuf;
//? }
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class UpgradedBoatEntity extends Boat implements HasCustomInventoryScreen, ContainerEntity,
    //? if fabric
    ExtendedScreenHandlerFactory
    //? if neoforge
    /*IMenuProviderExtension*/
{
    private static final EntityDataAccessor<Integer> TIER =
        SynchedEntityData.defineId(UpgradedBoatEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> EXTRA_SEATS =
        SynchedEntityData.defineId(UpgradedBoatEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<CompoundTag> SEATS =
        SynchedEntityData.defineId(UpgradedBoatEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private static final EntityDataAccessor<String> WOOD_ID =
        SynchedEntityData.defineId(UpgradedBoatEntity.class, EntityDataSerializers.STRING);

    public static final ResourceLocation DEFAULT_WOOD_ID = ModIds.of("minecraft", "oak");

    public static final float SEGMENT_SPACING = 1.75F;

    public static final int MAX_EXTRA_SEATS = 6;

    private NonNullList<ItemStack> inventory =
        NonNullList.withSize(BoatTier.COPPER.slots(), ItemStack.EMPTY);

    //? if >=1.21.1 {
    /*private @Nullable ResourceKey<LootTable> lootTableId;
    *///? } else {
    private @Nullable ResourceLocation lootTableId;
    //? }
    private long lootTableSeed;

    private int pendingSeat = -1;

    private float lastSafeYaw;
    private boolean lastSafeInitialized = false;

    private boolean destroying = false;
    private boolean partsBeingDiscarded = false;

    public boolean isDestroying() { return destroying; }
    public boolean arePartsBeingDiscarded() { return partsBeingDiscarded; }

    public UpgradedBoatEntity(EntityType<? extends Boat> type, Level world) {
        super(type, world);
    }

    public UpgradedBoatEntity(Level world, double x, double y, double z) {
        this(ModEntities.UPGRADED_BOAT, world);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    //? if >=1.21.1 {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TIER, BoatTier.COPPER.ordinal());
        builder.define(EXTRA_SEATS, 0);
        builder.define(SEATS, new CompoundTag());
        builder.define(WOOD_ID, DEFAULT_WOOD_ID.toString());
    }
    *///? } else {
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TIER, BoatTier.COPPER.ordinal());
        this.entityData.define(EXTRA_SEATS, 0);
        this.entityData.define(SEATS, new CompoundTag());
        this.entityData.define(WOOD_ID, DEFAULT_WOOD_ID.toString());
    }
    //? }

    public ResourceLocation getWoodId() {
        String s = this.entityData.get(WOOD_ID);
        return s == null || s.isEmpty() ? DEFAULT_WOOD_ID : ResourceLocation.tryParse(s);
    }

    public void setWoodId(ResourceLocation id) {
        if (id == null) id = DEFAULT_WOOD_ID;
        this.entityData.set(WOOD_ID, id.toString());
        net.cozystudios.tokimiboatyard.data.BoatWood wood =
            net.cozystudios.tokimiboatyard.data.BoatWoodRegistry.get(id);
        if (wood != null && wood.vanillaType() != null) {
            this.setVariant(wood.vanillaType());
        } else {
            this.setVariant(Boat.Type.OAK);
        }
    }


    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        LivingEntity firstLiving = null;
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof UpgradedBoatPart) continue;
            if (passenger instanceof Player player) return player;
            if (passenger instanceof LivingEntity living && firstLiving == null) firstLiving = living;
        }
        return firstLiving;
    }

    public BoatTier getTier() {
        return BoatTier.fromId(this.entityData.get(TIER));
    }

    public void setTier(BoatTier tier) {
        BoatTier previous = getTier();
        this.entityData.set(TIER, tier.ordinal());
        this.resizeInventoryForTier();
        if (!this.level().isClientSide && previous != tier) {
        }
    }


    public int getExtraSeats() {
        return this.entityData.get(EXTRA_SEATS);
    }

    public void setExtraSeats(int n) {
        int clamped = Math.max(0, Math.min(MAX_EXTRA_SEATS, n));
        int previous = getExtraSeats();
        if (clamped == previous) return;
        this.entityData.set(EXTRA_SEATS, clamped);
        if (!this.level().isClientSide) {
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
        NonNullList<ItemStack> old = this.inventory;
        NonNullList<ItemStack> fresh = NonNullList.withSize(newSize, ItemStack.EMPTY);
        int copyCount = Math.min(newSize, old.size());
        for (int i = 0; i < copyCount; i++) fresh.set(i, old.get(i));
        this.inventory = fresh;
        if (!this.level().isClientSide && old.size() > newSize) {
            for (int i = newSize; i < old.size(); i++) {
                if (!old.get(i).isEmpty()) {
                    Containers.dropItemStack(level(), getX(), getY() + 0.5, getZ(), old.get(i));
                }
            }
        }
    }


    public void createBoatParts() {
        if (this.level().isClientSide) return;
        int n = getExtraParts();
        for (int i = 1; i <= n; i++) {
            UpgradedBoatPart part = new UpgradedBoatPart(ModEntities.UPGRADED_BOAT_PART, this.level());
            part.setIndex(i);
            part.setOffsetZ(-i * SEGMENT_SPACING);
            part.setPos(this.getX(), this.getY(), this.getZ());
            this.level().addFreshEntity(part);
            part.startRiding(this, true);
        }
    }

    private void trimExcessParts(int keep) {
        this.partsBeingDiscarded = true;
        try {
            int kept = 0;
            for (Entity e : new ArrayList<>(this.getPassengers())) {
                if (!(e instanceof UpgradedBoatPart)) continue;
                if (kept < keep) {
                    kept++;
                } else {
                    e.stopRiding();
                    e.discard();
                }
            }
        } finally {
            this.partsBeingDiscarded = false;
        }
    }

    private void respawnParts() {
        this.partsBeingDiscarded = true;
        try {
            for (Entity e : new ArrayList<>(this.getPassengers())) {
                if (e instanceof UpgradedBoatPart) {
                    e.stopRiding();
                    e.discard();
                }
            }
        } finally {
            this.partsBeingDiscarded = false;
        }
        createBoatParts();
    }

    private long currentPartCount() {
        return this.getPassengers().stream().filter(e -> e instanceof UpgradedBoatPart).count();
    }


    private CompoundTag seatsNbt() { return this.entityData.get(SEATS); }

    public int getSeatOf(UUID id) {
        CompoundTag tag = seatsNbt();
        String key = id.toString();
        return tag.contains(key) ? tag.getInt(key) : -1;
    }

    private boolean hasSeat(UUID id) { return seatsNbt().contains(id.toString()); }

    private @Nullable UUID occupantOfSeat(int seat) {
        CompoundTag tag = seatsNbt();
        for (String key : tag.getAllKeys()) {
            if (tag.getInt(key) == seat) {
                try { return UUID.fromString(key); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    private boolean isSeatFree(int seat) { return occupantOfSeat(seat) == null; }

    private void putSeat(UUID id, int seat) {
        CompoundTag copy = seatsNbt().copy();
        copy.putInt(id.toString(), seat);
        this.entityData.set(SEATS, copy);
    }

    private void removeSeatEntry(UUID id) {
        CompoundTag tag = seatsNbt();
        String key = id.toString();
        if (!tag.contains(key)) return;
        CompoundTag copy = tag.copy();
        copy.remove(key);
        this.entityData.set(SEATS, copy);
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
        for (Entity p : this.getPassengers()) {
            if (p instanceof UpgradedBoatPart) continue;
            int seat = getSeatOf(p.getUUID());
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
        CompoundTag tag = seatsNbt();
        if (tag.isEmpty()) return;
        Set<String> aboard = new HashSet<>();
        for (Entity p : getPassengers()) {
            if (!(p instanceof UpgradedBoatPart)) aboard.add(p.getUUID().toString());
        }
        CompoundTag cleaned = new CompoundTag();
        boolean changed = false;
        for (String key : tag.getAllKeys()) {
            if (aboard.contains(key)) cleaned.putInt(key, tag.getInt(key));
            else changed = true;
        }
        int max = getTotalSeats();
        CompoundTag cleaned2 = new CompoundTag();
        for (String key : cleaned.getAllKeys()) {
            int seat = cleaned.getInt(key);
            if (seat < max) cleaned2.putInt(key, seat);
            else changed = true;
        }
        if (changed) this.entityData.set(SEATS, cleaned2);
    }


    @Override
    protected int getMaxPassengers() {
        return getTotalSeats() + getExtraParts();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        if (passenger instanceof UpgradedBoatPart) return true;
        long regulars = this.getPassengers().stream()
            .filter(e -> !(e instanceof UpgradedBoatPart)).count();
        //? if fabric {
        return regulars < getTotalSeats() && !this.isEyeInFluid(FluidTags.WATER);
        //?} else if neoforge {
        /*return regulars < getTotalSeats() && this.getEyeInFluidType() != NeoForgeMod.WATER_TYPE.value();
        *///?}
    }

    @Override
    protected void addPassenger(Entity passenger) {
        if (passenger instanceof UpgradedBoatPart part && isExcessPart()) {
            dismissExcessPart(part);
            return;
        }
        super.addPassenger(passenger);
        if (passenger instanceof UpgradedBoatPart) return;
        if (this.level().isClientSide) return;
        UUID id = passenger.getUUID();
        if (hasSeat(id)) return;
        int seat = -1;
        if (this.pendingSeat >= 0 && isSeatFree(this.pendingSeat)) seat = this.pendingSeat;
        if (seat < 0) seat = firstFreeSeat();
        if (seat >= 0) putSeat(id, seat);
    }

    private boolean isExcessPart() {
        int count = 0;
        for (Entity p : this.getPassengers()) {
            if (p instanceof UpgradedBoatPart) count++;
        }
        return count >= getExtraParts();
    }

    private void dismissExcessPart(UpgradedBoatPart part) {
        if (this.level().isClientSide) return;
        this.partsBeingDiscarded = true;
        try {
            part.stopRiding();
            part.discard();
        } finally {
            this.partsBeingDiscarded = false;
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger instanceof UpgradedBoatPart) return;
        if (this.level().isClientSide) return;
        removeSeatEntry(passenger.getUUID());
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction updater) {
        if (!this.hasPassenger(passenger)) return;

        if (passenger instanceof UpgradedBoatPart part) {
            Vec3 local = new Vec3(0.0, 0.0, part.getOffsetZ())
                .yRot(-this.getYRot() * ((float) Math.PI / 180f));
            updater.accept(passenger,
                this.getX() + local.x,
                this.getY(),
                this.getZ() + local.z);
            passenger.setYRot(this.getYRot());
            passenger.setYHeadRot(this.getYRot());
            return;
        }

        int seatIdx = getSeatOf(passenger.getUUID());
        if (seatIdx < 0) seatIdx = regularPassengerIndex(passenger);

        int segment = seatIdx / 2;
        int seatInSeg = seatIdx % 2;
        float zInSeg = seatInSeg == 0 ? 0.2f : -0.6f;
        float localZ = -segment * SEGMENT_SPACING + zInSeg;

        //? if >=1.21.1 {
        /*double ridingOffset = this.getPassengerRidingPosition(passenger).y - this.getY()
            - passenger.getVehicleAttachmentPoint(this).y;
        *///? } else {
        double ridingOffset = this.getPassengersRidingOffset() + passenger.getMyRidingOffset();
        //? }
        Vec3 local = new Vec3(0.0, ridingOffset, localZ)
            .yRot(-this.getYRot() * ((float) Math.PI / 180f));
        updater.accept(passenger,
            this.getX() + local.x,
            this.getY() + local.y,
            this.getZ() + local.z);
        clampPassengerYawInline(passenger);
        if (passenger instanceof net.minecraft.world.entity.animal.Animal animal
            && this.regularPassengerCount() > 1) {
            int rotation = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setYBodyRot(animal.yBodyRot + rotation);
            passenger.setYHeadRot(passenger.getYHeadRot() + rotation);
        }
    }

    private int regularPassengerIndex(Entity passenger) {
        int idx = 0;
        for (Entity p : this.getPassengers()) {
            if (p instanceof UpgradedBoatPart) continue;
            if (p == passenger) return idx;
            idx++;
        }
        return 0;
    }

    private int regularPassengerCount() {
        int n = 0;
        for (Entity p : this.getPassengers()) if (!(p instanceof UpgradedBoatPart)) n++;
        return n;
    }

    private void clampPassengerYawInline(Entity passenger) {
        passenger.setYBodyRot(this.getYRot());
        float delta = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
        float clamped = Mth.clamp(delta, -105.0F, 105.0F);
        passenger.yRotO += clamped - delta;
        passenger.setYRot(passenger.getYRot() + clamped - delta);
        passenger.setYHeadRot(passenger.getYRot());
    }


    @Override
    public void tick() {
        if (getTier().fireproof() && !this.level().isClientSide) {
            this.setRemainingFireTicks(0);
        }
        super.tick();
        if (getTier().fireproof()) {
            applyLavaBuoyancy();
        }

        if (!this.level().isClientSide && this.isAlive() && this.tickCount > 20 && this.tickCount % 20 == 0) {
            int expected = getExtraParts();
            long actual = currentPartCount();
            if (actual < expected) {
                respawnParts();
            } else if (actual > expected) {
                trimExcessParts(expected);
            }
            pruneStaleSeats();
        }

        if (!this.level().isClientSide) {
            autoMountNearby();
        }

        if (!this.level().isClientSide && getExtraParts() > 0 && this.isAlive()) {
            validateNoClipOrRollback();
        }
    }

    private void validateNoClipOrRollback() {
        this.setBoundingBox(makeBoundingBox());
        boolean overlapsTerrain =
            this.level().getBlockCollisions(this, this.getBoundingBox()).iterator().hasNext();

        if (!overlapsTerrain) {
            this.lastSafeYaw = this.getYRot();
            this.lastSafeInitialized = true;
            return;
        }

        if (!this.lastSafeInitialized) return;

        super.setYRot(this.lastSafeYaw);
        this.setBoundingBox(makeBoundingBox());
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof UpgradedBoatPart) {
                this.positionRider(passenger, Entity::setPosRaw);
            }
        }
    }

    public void autoMountNearby() {
        AABB expanded = this.getBoundingBox().inflate(0.75, 0.0, 0.75);
        List<Entity> nearby = this.level().getEntities(this, expanded,
            e -> !e.isSpectator() && e.isPickable());
        for (Entity e : nearby) {
            if (e.isPassenger()) continue;
            if (e instanceof UpgradedBoatPart) continue;
            if (e instanceof Player) continue;
            if (!(e instanceof LivingEntity)) continue;
            if (!(e instanceof AgeableMob)) continue;
            if (e instanceof WaterAnimal) continue;
            if (e.getBbWidth() >= this.getBbWidth()) continue;
            if (!canAddPassenger(e)) continue;

            int segment = closestSegment(e);
            int seat = firstFreeSeatInSegment(segment);
            if (seat < 0) seat = firstFreeSeat();
            if (seat < 0) continue;
            setPendingSeat(seat);
            e.startRiding(this);
            setPendingSeat(-1);
        }
    }

    private int closestSegment(Entity e) {
        float yawRad = this.getYRot() * ((float) Math.PI / 180F);
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);
        int bestSegment = 0;
        double bestDistSq = Double.MAX_VALUE;
        int totalSegments = getExtraParts() + 1;
        for (int i = 0; i < totalSegments; i++) {
            double segX = this.getX() + i * SEGMENT_SPACING * sin;
            double segZ = this.getZ() - i * SEGMENT_SPACING * cos;
            double dx = e.getX() - segX;
            double dz = e.getZ() - segZ;
            double distSq = dx * dx + dz * dz;
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                bestSegment = i;
            }
        }
        return bestSegment;
    }

    private void applyLavaBuoyancy() {
        BlockPos here = blockPosition();
        BlockPos below = here.below();
        boolean inLava = this.level().getFluidState(here).is(FluidTags.LAVA);
        boolean overLava = this.level().getFluidState(below).is(FluidTags.LAVA);
        if (!inLava && !overLava) return;
        Vec3 v = this.getDeltaMovement();
        double vy = v.y;
        if (inLava) {
            vy = Math.max(vy + 0.04, 0.03);
            vy = Math.min(vy, 0.12);
        } else if (vy < 0) {
            vy = 0;
        }
        this.setDeltaMovement(v.x * 0.9, vy, v.z * 0.9);
        this.fallDistance = 0;
    }

    @Override
    public boolean fireImmune() {
        return getTier().fireproof() || super.fireImmune();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getTier().fireproof() && source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return super.hurt(source, amount);
    }


    @Override
    public NonNullList<ItemStack> getItemStacks() { return this.inventory; }

    @Override
    public void clearItemStacks() {
        this.inventory = NonNullList.withSize(getTier().slots(), ItemStack.EMPTY);
    }

    //? if >=1.21.1 {
    /*@Override
    public @Nullable ResourceKey<LootTable> getLootTable() { return this.lootTableId; }
    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> id) { this.lootTableId = id; }
    *///? } else {
    @Override
    public @Nullable ResourceLocation getLootTable() { return this.lootTableId; }
    @Override
    public void setLootTable(@Nullable ResourceLocation id) { this.lootTableId = id; }
    //? }
    @Override
    public long getLootTableSeed() { return this.lootTableSeed; }
    @Override
    public void setLootTableSeed(long seed) { this.lootTableSeed = seed; }
    @Override
    public int getContainerSize() { return this.inventory.size(); }

    @Override
    public boolean stillValid(Player player) {
        return !this.isRemoved() && this.distanceToSqr(player) <= 64.0;
    }

    @Override
    public void setChanged() {}

    @Override
    public void clearContent() { this.inventory.clear(); }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) { return this.inventory.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(this.inventory, slot, amount); }

    @Override
    public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(this.inventory, slot); }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }


    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return interactSegment(player, hand, pickedSegmentForPlayer(player));
    }

    private int pickedSegmentForPlayer(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 worldRayEnd = eyePos.add(lookVec.scale(15.0));

        float yawRad = getYRot() * ((float) Math.PI / 180F);
        Vec3 headPos = new Vec3(this.getX(), this.getY(), this.getZ());
        Vec3 localEye = eyePos.subtract(headPos).yRot(yawRad);
        Vec3 localEnd = worldRayEnd.subtract(headPos).yRot(yawRad);

        int bestSegment = 0;
        double bestDistSq = Double.MAX_VALUE;
        boolean anyHit = false;

        int totalSegments = getExtraParts() + 1;
        double halfSeg = SEGMENT_SPACING / 2.0;
        for (int i = 0; i < totalSegments; i++) {
            double centerZ = -i * SEGMENT_SPACING;
            AABB localBox = new AABB(
                -0.75, 0.0, centerZ - halfSeg,
                 0.75, 0.5625, centerZ + halfSeg
            );
            Optional<Vec3> hit = localBox.clip(localEye, localEnd);
            if (hit.isEmpty()) continue;
            double d = localEye.distanceToSqr(hit.get());
            if (!anyHit || d < bestDistSq) {
                bestDistSq = d;
                bestSegment = i;
                anyHit = true;
            }
        }
        return bestSegment;
    }

    public InteractionResult interactSegment(Player player, InteractionHand hand, int segmentIndex) {
        if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
            int seat = firstFreeSeatInSegment(segmentIndex);
            if (seat < 0) seat = firstFreeSeat();
            if (seat >= 0) {
                if (this.level().isClientSide) return InteractionResult.SUCCESS;
                this.pendingSeat = seat;
                boolean ok = player.startRiding(this);
                this.pendingSeat = -1;
                return ok ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
        }
        InteractionResult result = this.interactWithContainerVehicle(player);
        if (result.consumesAction() && !this.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinAi.angerNearbyPiglins(player, true);
        }
        return result;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        InteractionResult result = this.interactWithContainerVehicle(player);
        if (result.consumesAction() && !this.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinAi.angerNearbyPiglins(player, true);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 dismountOffset = Boat.getCollisionHorizontalEscapeVector(
            this.getBbWidth() * Mth.SQRT_OF_TWO,
            passenger.getBbWidth(),
            passenger.getYRot());
        double dx = passenger.getX() + dismountOffset.x;
        double dz = passenger.getZ() + dismountOffset.z;
        BlockPos posTop = BlockPos.containing(dx, this.getBoundingBox().maxY, dz);
        BlockPos posBelow = posTop.below();
        if (!this.level().isWaterAt(posBelow)) {
            List<Vec3> candidates = new ArrayList<>();
            double topH = this.level().getBlockFloorHeight(posTop);
            if (DismountHelper.isBlockFloorValid(topH)) {
                candidates.add(new Vec3(dx, posTop.getY() + topH, dz));
            }
            double belowH = this.level().getBlockFloorHeight(posBelow);
            if (DismountHelper.isBlockFloorValid(belowH)) {
                candidates.add(new Vec3(dx, posBelow.getY() + belowH, dz));
            }
            for (Pose pose : passenger.getDismountPoses()) {
                for (Vec3 candidate : candidates) {
                    if (DismountHelper.canDismountTo(this.level(), candidate, passenger, pose)) {
                        passenger.setPose(pose);
                        return candidate;
                    }
                }
            }
        }
        return new Vec3(passenger.getX(), this.getBoundingBox().maxY, passenger.getZ());
    }

    @Override
    public void remove(RemovalReason reason) {
        this.destroying = true;
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }
        if (!this.level().isClientSide) {
            for (Entity e : new ArrayList<>(this.getPassengers())) {
                if (e instanceof UpgradedBoatPart) {
                    e.stopRiding();
                    e.discard();
                }
            }
        }
        super.remove(reason);
    }

    @Override
    protected void destroy(DamageSource source) {
        if (!this.level().isClientSide) {
            this.spawnAtLocation(createItemStackWithNbt());
        }
    }


    private static final double PART_HALF_WIDTH = 0.5;

    @Override
    protected AABB makeBoundingBox() {
        AABB head = super.makeBoundingBox();
        int extraParts = getExtraParts();
        if (extraParts == 0) return head;

        double minX = head.minX, maxX = head.maxX;
        double minY = head.minY, maxY = head.maxY;
        double minZ = head.minZ, maxZ = head.maxZ;

        float rad = getYRot() * ((float) Math.PI / 180F);
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
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        this.setBoundingBox(makeBoundingBox());
        super.move(type, movement);
    }

    @Override
    public void setYRot(float yaw) {
        if (this.level() == null || getExtraParts() == 0) {
            super.setYRot(yaw);
            return;
        }
        if (yaw == this.getYRot()) {
            return;
        }
        float oldYaw = this.getYRot();
        AABB oldBox = makeBoundingBox();
        super.setYRot(yaw);
        AABB newBox = makeBoundingBox();
        boolean newOverlaps =
            this.level().getBlockCollisions(this, newBox).iterator().hasNext();
        if (!newOverlaps) return;
        boolean oldOverlaps =
            this.level().getBlockCollisions(this, oldBox).iterator().hasNext();
        if (!oldOverlaps) {
            super.setYRot(oldYaw);
        }
    }

    public ItemStack createItemStackWithNbt() {
        ItemStack stack = new ItemStack(this.getDropItem());
        int extras = getExtraSeats();
        if (extras > 0) {
            UpgradedBoatItem.writeExtraSeats(stack, extras);
        }
        return stack;
    }

    @Override
    public ItemStack getPickResult() {
        return createItemStackWithNbt();
    }

    @Override
    public Item getDropItem() { return ModItems.get(getTier(), getWoodId()); }


    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Tier", getTier().ordinal());
        nbt.putInt("ExtraSeats", getExtraSeats());
        nbt.put("Seats", seatsNbt().copy());
        nbt.putString("WoodId", getWoodId().toString());
        //? if >=1.21.1 {
        /*this.addChestVehicleSaveData(nbt, this.registryAccess());
        *///? } else {
        this.addChestVehicleSaveData(nbt);
        //? }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("Tier", Tag.TAG_INT)) {
            BoatTier tier = BoatTier.fromId(nbt.getInt("Tier"));
            this.entityData.set(TIER, tier.ordinal());
            this.inventory = NonNullList.withSize(tier.slots(), ItemStack.EMPTY);
        }
        if (nbt.contains("ExtraSeats", Tag.TAG_INT)) {
            int val = Math.max(0, Math.min(MAX_EXTRA_SEATS, nbt.getInt("ExtraSeats")));
            this.entityData.set(EXTRA_SEATS, val);
        }
        if (nbt.contains("Seats", Tag.TAG_COMPOUND)) {
            this.entityData.set(SEATS, nbt.getCompound("Seats").copy());
        }
        if (nbt.contains("WoodId", Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(nbt.getString("WoodId"));
            if (id != null) setWoodId(id);
        }
        //? if >=1.21.1 {
        /*this.readChestVehicleSaveData(nbt, this.registryAccess());
        *///? } else {
        this.readChestVehicleSaveData(nbt);
        //? }
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.tokimiboatyard.upgraded_boat." + getTier().id());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        if (this.lootTableId != null && player.isSpectator()) return null;
        this.unpackChestVehicleLootTable(player);
        return new UpgradedBoatScreenHandler(syncId, inv, this, getTier().rows());
    }

    //? if fabric && >=1.21.1 {
    /*@Override
    public Integer getScreenOpeningData(ServerPlayer player) {
        return getTier().rows();
    }
    *///?} else if fabric {
    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeByte(getTier().rows());
    }
    //?}
    //? if neoforge {
    /*@Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(getTier().rows());
    }
    *///?}
}
