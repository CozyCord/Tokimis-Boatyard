package net.cozystudios.tokimiboatyard.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
//? if >=1.21.1 {
/*import net.minecraft.server.level.ServerEntity;
*///? }
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class UpgradedBoatPart extends Entity {
    private static final EntityDataAccessor<Integer> INDEX =
        SynchedEntityData.defineId(UpgradedBoatPart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> OFFSET_Z =
        SynchedEntityData.defineId(UpgradedBoatPart.class, EntityDataSerializers.FLOAT);

    private int lifelessCountdown = 20;

    public UpgradedBoatPart(EntityType<? extends UpgradedBoatPart> type, Level world) {
        super(type, world);
        this.blocksBuilding = true;
    }

    //? if >=1.21.1 {
    /*@Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(INDEX, 0);
        builder.define(OFFSET_Z, 0f);
    }
    *///? } else {
    @Override
    protected void defineSynchedData() {
        this.entityData.define(INDEX, 0);
        this.entityData.define(OFFSET_Z, 0f);
    }
    //? }

    public int getIndex() { return entityData.get(INDEX); }
    public void setIndex(int i) { entityData.set(INDEX, i); }

    public float getOffsetZ() { return entityData.get(OFFSET_Z); }
    public void setOffsetZ(float z) { entityData.set(OFFSET_Z, z); }

    public @Nullable UpgradedBoatEntity getParentBoat() {
        return getVehicle() instanceof UpgradedBoatEntity boat ? boat : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (lifelessCountdown > 0) lifelessCountdown--;
        if (lifelessCountdown <= 0 && getParentBoat() == null) {
            this.discard();
        }
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        if (this.isPassenger()) return;
        super.move(type, movement);
    }

    @Override
    public void onInsideBubbleColumn(boolean drag) {}

    @Override
    public void onAboveBubbleCol(boolean drag) {}

    @Override
    public void stopRiding() {
        if (this.level() != null && this.level().isClientSide) {
            super.stopRiding();
            return;
        }
        Entity previous = this.getVehicle();
        if (previous instanceof UpgradedBoatEntity parent
            && parent.isAlive()
            && !parent.isDestroying()
            && !parent.arePartsBeingDiscarded()) {
            return;
        }
        super.stopRiding();
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        UpgradedBoatEntity parent = getParentBoat();
        if (parent != null) parent.hurt(source, amount);
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        UpgradedBoatEntity parent = getParentBoat();
        return parent != null
            ? parent.interactSegment(player, hand, getIndex())
            : super.interact(player, hand);
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return (other.canBeCollidedWith() || other.isPushable()) && !this.isPassengerOfSameVehicle(other);
    }

    @Override
    public boolean canBeCollidedWith() { return true; }

    @Override
    public boolean isPushable() { return true; }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public Vec3 getDeltaMovement() {
        UpgradedBoatEntity parent = getParentBoat();
        return parent != null ? parent.getDeltaMovement() : super.getDeltaMovement();
    }


    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        if (nbt.contains("Index")) setIndex(nbt.getInt("Index"));
        if (nbt.contains("OffsetZ")) setOffsetZ(nbt.getFloat("OffsetZ"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Index", getIndex());
        nbt.putFloat("OffsetZ", getOffsetZ());
    }

    //? if >=1.21.1 {
    /*@Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }
    *///? } else {
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
        //? }
}
