package net.cozystudios.tokimiboatyard.item;

import net.cozystudios.tokimiboatyard.entity.BoatTier;
import net.cozystudios.tokimiboatyard.entity.UpgradedBoatEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class UpgradedBoatItem extends Item {
    private static final Predicate<Entity> RIDERS = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::canHit);

    private final BoatTier tier;
    private final BoatEntity.Type woodType;

    public UpgradedBoatItem(BoatTier tier, BoatEntity.Type woodType, Item.Settings settings) {
        super(settings);
        this.tier = tier;
        this.woodType = woodType;
    }

    public BoatTier getTier() { return tier; }
    public BoatEntity.Type getWoodType() { return woodType; }

    public static int readExtraSeats(ItemStack stack) {
        if (!stack.hasNbt()) return 0;
        return Math.max(0, Math.min(6, stack.getNbt().getInt("ExtraSeats")));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal(tier.displayName() + " Tier")
            .styled(s -> s.withColor(TextColor.fromRgb(tier.tooltipColor()))));
        int extras = readExtraSeats(stack);
        if (extras > 0) {
            int total = 2 + extras;
            tooltip.add(Text.literal("+" + extras + " Seat" + (extras == 1 ? "" : "s") + " (" + total + " total)")
                .styled(s -> s.withColor(TextColor.fromRgb(0xBBBBBB))));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        HitResult hit = Item.raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (hit.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(stack);
        }
        Vec3d rotation = user.getRotationVec(1.0F);
        List<Entity> nearby = world.getOtherEntities(user,
            user.getBoundingBox().stretch(rotation.multiply(5.0)).expand(1.0),
            RIDERS);
        if (!nearby.isEmpty()) {
            Vec3d eyePos = user.getEyePos();
            for (Entity e : nearby) {
                Box box = e.getBoundingBox().expand(e.getTargetingMargin());
                if (box.contains(eyePos)) {
                    return TypedActionResult.pass(stack);
                }
            }
        }
        if (hit.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(stack);
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        UpgradedBoatEntity boat = new UpgradedBoatEntity(world, hit.getPos().x, hit.getPos().y, hit.getPos().z);
        boat.setVariant(this.woodType);
        boat.setTier(this.tier);
        boat.setExtraSeats(readExtraSeats(stack));
        boat.setYaw(user.getYaw());
        if (!world.isSpaceEmpty(boat, boat.getBoundingBox())) {
            return TypedActionResult.fail(stack);
        }
        if (!world.isClient) {
            world.spawnEntity(boat);
            boat.createBoatParts();
            world.emitGameEvent(user, GameEvent.ENTITY_PLACE, blockHit.getPos());
            if (!user.getAbilities().creativeMode) stack.decrement(1);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(stack, world.isClient);
    }
}
