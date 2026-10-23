package net.cozystudios.tokimiboatyard.item;

import net.cozystudios.tokimiboatyard.data.BoatWood;
import net.cozystudios.tokimiboatyard.data.BoatWoodRegistry;
import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.entity.UpgradedBoatEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.1 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
*///? }
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class UpgradedBoatItem extends Item {
    private static final Predicate<Entity> RIDERS = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    private final BoatTier tier;
    private final ResourceLocation woodId;

    public UpgradedBoatItem(BoatTier tier, ResourceLocation woodId, Item.Properties settings) {
        super(settings);
        this.tier = tier;
        this.woodId = woodId;
    }

    public BoatTier getTier() { return tier; }
    public ResourceLocation getWoodId() { return woodId; }

    public @Nullable BoatWood getWood() { return BoatWoodRegistry.get(woodId); }

    public static int readExtraSeats(ItemStack stack) {
        //? if >=1.21.1 {
        /*CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        return Math.max(0, Math.min(6, data.copyTag().getInt("ExtraSeats")));
        *///? } else {
        if (!stack.hasTag()) return 0;
        return Math.max(0, Math.min(6, stack.getTag().getInt("ExtraSeats")));
        //? }
    }

    public static void writeExtraSeats(ItemStack stack, int extras) {
        //? if >=1.21.1 {
        /*CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA) == null
            ? new CompoundTag() : stack.get(DataComponents.CUSTOM_DATA).copyTag();
        tag.putInt("ExtraSeats", extras);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        *///? } else {
        stack.getOrCreateTag().putInt("ExtraSeats", extras);
        //? }
    }

    public static void removeExtraSeats(ItemStack stack) {
        //? if >=1.21.1 {
        /*CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag tag = data.copyTag();
        tag.remove("ExtraSeats");
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        *///? } else {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            tag.remove("ExtraSeats");
            if (tag.isEmpty()) stack.setTag(null);
        }
        //? }
    }

    @Override
    //? if >=1.21.1 {
    /*public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    *///? } else {
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
    //? }
        tooltip.add(Component.literal(tier.displayName() + " Tier")
            .withStyle(s -> s.withColor(TextColor.fromRgb(tier.tooltipColor()))));
        int extras = readExtraSeats(stack);
        if (extras > 0) {
            int total = 2 + extras;
            tooltip.add(Component.literal("+" + extras + " Seat" + (extras == 1 ? "" : "s") + " (" + total + " total)")
                .withStyle(s -> s.withColor(TextColor.fromRgb(0xBBBBBB))));
        }
        super.appendHoverText(stack,
            //? if >=1.21.1 {
            /*context, tooltip, flag);
            *///? } else {
            world, tooltip, context);
            //? }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        HitResult hit = Item.getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);
        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }
        Vec3 rotation = user.getViewVector(1.0F);
        List<Entity> nearby = world.getEntities(user,
            user.getBoundingBox().expandTowards(rotation.scale(5.0)).inflate(1.0),
            RIDERS);
        if (!nearby.isEmpty()) {
            Vec3 eyePos = user.getEyePosition();
            for (Entity e : nearby) {
                AABB box = e.getBoundingBox().inflate(e.getPickRadius());
                if (box.contains(eyePos)) {
                    return InteractionResultHolder.pass(stack);
                }
            }
        }
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        UpgradedBoatEntity boat = new UpgradedBoatEntity(world, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
        boat.setWoodId(this.woodId);
        boat.setTier(this.tier);
        boat.setExtraSeats(readExtraSeats(stack));
        boat.setYRot(user.getYRot());
        if (!world.noCollision(boat, boat.getBoundingBox())) {
            return InteractionResultHolder.fail(stack);
        }
        if (!world.isClientSide) {
            world.addFreshEntity(boat);
            boat.createBoatParts();
            world.gameEvent(user, GameEvent.ENTITY_PLACE, blockHit.getLocation());
            if (!user.getAbilities().instabuild) stack.shrink(1);
        }
        user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }
}
