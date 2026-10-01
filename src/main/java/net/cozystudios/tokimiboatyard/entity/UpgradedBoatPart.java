package net.cozystudios.tokimiboatyard.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradedBoatPart extends Entity {
    private static final TrackedData<Integer> INDEX =
        DataTracker.registerData(UpgradedBoatPart.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> OFFSET_Z =
        DataTracker.registerData(UpgradedBoatPart.class, TrackedDataHandlerRegistry.FLOAT);

    private int lifelessCountdown = 20;

    public UpgradedBoatPart(EntityType<? extends UpgradedBoatPart> type, World world) {
        super(type, world);
        this.intersectionChecked = true;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(INDEX, 0);
        this.dataTracker.startTracking(OFFSET_Z, 0f);
    }

    public int getIndex() { return dataTracker.get(INDEX); }
    public void setIndex(int i) { dataTracker.set(INDEX, i); }

    public float getOffsetZ() { return dataTracker.get(OFFSET_Z); }
    public void setOffsetZ(float z) { dataTracker.set(OFFSET_Z, z); }

    public @Nullable UpgradedBoatEntity getParentBoat() {
        return getVehicle() instanceof UpgradedBoatEntity boat ? boat : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (lifelessCountdown > 0) lifelessCountdown--;
        if (lifelessCountdown <= 0 && getParentBoat() == null) {
            this.discard();
            return;
        }

        UpgradedBoatEntity parent = getParentBoat();
        if (parent == null || getWorld().isClient) return;
        Box searchBox = getBoundingBox().expand(0.2, -0.01, 0.2);
        List<Entity> nearby = getWorld().getOtherEntities(this, searchBox,
            e -> !e.isSpectator() && e.canHit());
        for (Entity e : nearby) {
            if (e.hasVehicle()) continue;
            if (e instanceof UpgradedBoatPart) continue;
            if (e instanceof PlayerEntity) continue;
            if (!(e instanceof LivingEntity)) continue;
            if (!(e instanceof PassiveEntity)) continue;
            if (e instanceof WaterCreatureEntity) continue;
            if (e.getWidth() >= this.getWidth()) continue;
            if (!parent.canAddPassenger(e)) continue;

            int seat = parent.firstFreeSeatInSegment(getIndex());
            if (seat < 0) seat = parent.firstFreeSeat();
            if (seat < 0) continue;
            parent.setPendingSeat(seat);
            e.startRiding(parent);
            parent.setPendingSeat(-1);
        }
    }


    @Override
    public boolean damage(DamageSource source, float amount) {
        UpgradedBoatEntity parent = getParentBoat();
        if (parent != null) parent.damage(source, amount);
        return false;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        UpgradedBoatEntity parent = getParentBoat();
        return parent != null
            ? parent.interactSegment(player, hand, getIndex())
            : super.interact(player, hand);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return (other.isCollidable() || other.isPushable()) && !this.isConnectedThroughVehicle(other);
    }

    @Override
    public boolean isCollidable() { return true; }

    @Override
    public boolean isPushable() { return true; }

    @Override
    public boolean canHit() { return true; }

    @Override
    public Vec3d getVelocity() {
        UpgradedBoatEntity parent = getParentBoat();
        return parent != null ? parent.getVelocity() : super.getVelocity();
    }


    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Index")) setIndex(nbt.getInt("Index"));
        if (nbt.contains("OffsetZ")) setOffsetZ(nbt.getFloat("OffsetZ"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Index", getIndex());
        nbt.putFloat("OffsetZ", getOffsetZ());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
