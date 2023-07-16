package com.fotatata.environmental_expansion.entity.custom;

import com.fotatata.environmental_expansion.entity.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CraneEntity extends Animal implements GeoEntity {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(ItemTags.FISHES);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CraneEntity(EntityType<? extends Animal> p_27557_, Level p_27558_) {
        super(p_27557_, p_27558_);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @Nullable AgeableMob mob) {
        return ModEntityTypes.CRANE.get().create(level);
    }

    public static AttributeSupplier setAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20d)
                .add(Attributes.MOVEMENT_SPEED, 0.4d).build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.8d));
        this.goalSelector.addGoal(2, new TemptGoal(this, 0.7d, FOOD_ITEMS, false));
        this.goalSelector.addGoal(3, new BreedGoal(this, 0.6d));
        this.goalSelector.addGoal(4, new SeekWaterGoal(this, 0.6d));
        this.goalSelector.addGoal(4, new AvoidWaterGoal(this,0.6d));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6f));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class,6.0f));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 0.7d));
    }

    public boolean isFood(@NotNull ItemStack p_28271_) {
        return FOOD_ITEMS.test(p_28271_);
    }

    @Override
    protected float getWaterSlowDown() {
        return this.isPathFinding() ? 0.95f : 0.8f;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                DefaultAnimations.genericWalkIdleController(this)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static class SeekWaterGoal extends Goal {
        protected CraneEntity crane;
        protected double speedModifier;
        protected BlockPos targetPos;
        protected int coolDownCounter;

        public SeekWaterGoal(CraneEntity crane, double speedModifier) {
            this.crane = crane;
            this.speedModifier = speedModifier;
        }

        @Override
        public boolean canUse() {
            if (coolDownCounter > 0) {
                coolDownCounter--;
                return false;
            }
            targetPos = getTargetPosition(crane.level(), crane.blockPosition());
            return targetPos != null && !isValidPosition(crane.getOnPos(), crane.level());
        }

        @Override
        public void tick() {
            if (!crane.getNavigation().isStuck()) {
                crane.getNavigation().moveTo(this.targetPos.above().getCenter().x, this.targetPos.above().getCenter().y, this.targetPos.above().getCenter().z, speedModifier);
                if (crane.isInWater()) coolDownCounter = (int) (Math.random() * 1200 + 300);
            }
        }

        protected BlockPos getTargetPosition(Level level, BlockPos pos) {
            for (int x = 0; x <= 48; x++) {
                for (int z = 0; z <= 48; z++) {
                    for (int y = 0; y <= 12; y++) {
                        BlockPos current = pos.offset(
                                x % 2 == 0 ? x / 2 : (-x - 1) / 2,
                                y % 2 == 0 ? y / 2 : (-y - 1) / 2,
                                z % 2 == 0 ? z / 2 : (-z - 1) / 2);
                        if (isValidPosition(current, level)) return current;
                    }
                }
            }
            return null;
        }

        protected boolean isValidWater(BlockPos pos, Level level) {
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                BlockState state = level.getBlockState(pos);
                return state.is(Blocks.WATER) || state.is(Blocks.SEAGRASS) || state.is(BlockTags.CORALS);
            }
            return false;
        }

        protected boolean isValidPosition(BlockPos pos, Level level) {
            return isValidWater(pos, level) &&
                    isValidWater(pos.north(), level) &&
                    isValidWater(pos.east(), level) &&
                    isValidWater(pos.south(), level) &&
                    isValidWater(pos.west(), level) &&
                    level.getBlockState(pos.above()).isAir() &&
                    !isValidWater(pos.below(), level);
        }
    }

    public static class AvoidWaterGoal extends Goal {
        protected final double speedModifier;
        protected double xVelocity;
        protected double yVelocity;
        protected final CraneEntity crane;
        protected Vec3 deltaMovement;

        protected BlockPos position;

        public boolean canUse() {
            position = getTargetPosition(crane.level(), crane.getOnPos());
            if (position != null && ((this.crane.isInWater() && !this.crane.isBaby() && (this.crane.getFluidTypeHeight(Fluids.WATER.getFluidType()) > 1D)) || (crane.isBaby() && crane.isInWater()))) {
                xVelocity = 0;
                yVelocity = 0.5d;
                deltaMovement = crane.getDeltaMovement();
                return true;}
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return  true;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public AvoidWaterGoal(CraneEntity crane, double speedModifier) {
            this.crane = crane;
            this.speedModifier = speedModifier;
        }

        protected boolean isValidPosition(BlockPos pos, Level level) {
            return !level.getFluidState(pos).is(FluidTags.LAVA) && level.getBlockState(pos.above()).isAir() && !level.getFluidState(pos).is(Fluids.WATER) && !level.getBlockState(pos).isAir();
        }

        protected BlockPos getTargetPosition(Level level, BlockPos pos) {
            for (int x = 24; x <= 48; x++) {
                for (int z = 24; z <= 48; z++) {
                    for (int y = 0; y <= 24; y++) {
                        BlockPos current = pos.offset(
                                x % 2 == 0 ? x / 2 : (-x - 1) / 2,
                                y % 2 == 0 ? y / 2 : (-y - 1) / 2,
                                z % 2 == 0 ? z / 2 : (-z - 1) / 2);
                        if (isValidPosition(current, level)) return current;
                    }
                }
            }
            return null;
        }

        @Override
        public void tick() {
            crane.getNavigation().moveTo(position.getX(), position.getY(), position.getZ(), xVelocity);
            if (xVelocity < speedModifier) xVelocity += 0.1d;
            System.out.println(xVelocity);
            crane.setDeltaMovement(deltaMovement.x,yVelocity,deltaMovement.z);
            if (yVelocity > 0) yVelocity -= 0.1;
            System.out.println(yVelocity);

            if (crane.getOnPos() == position) crane.setNoGravity(false);
        }

        @Override
        public void start() {
        }
    }
}
