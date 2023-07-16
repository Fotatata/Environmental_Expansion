package com.fotatata.environmental_expansion.entity.custom;

import com.fotatata.environmental_expansion.entity.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BeaverEntity extends Animal implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1,new FloatGoal(this));
        this.goalSelector.addGoal(1,new PanicGoal(this,0.8d));
        this.goalSelector.addGoal(2, new BreedGoal(this,0.5d));
        this.goalSelector.addGoal(2, new TemptGoal(this,0.6d, Ingredient.of(ItemTags.SAPLINGS),false));
        this.goalSelector.addGoal(3,new FollowParentGoal(this, 0.6d));
        this.goalSelector.addGoal(4, new BeaverDestroySapling(this, BlockTags.SAPLINGS,0.5d));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this,0.4d));
        this.goalSelector.addGoal(5,new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6,new LookAtPlayerGoal(this, Player.class,6.0f));
    }

    public BeaverEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier setAttributes(){
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH,6d)
                .add(Attributes.MOVEMENT_SPEED, 0.4d).build();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level,@Nullable AgeableMob ageableMob) {
        return ModEntityTypes.BEAVER.get().create(level);
    }

    public boolean isFood(@NotNull ItemStack p_28271_) {return Ingredient.of(ItemTags.SAPLINGS).test(p_28271_);}

    public static final EntityDataAccessor<Boolean> isGnawing = SynchedEntityData.defineId(BeaverEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(isGnawing, false);
    }

    public void setGnawAnim(boolean state){
        this.entityData.set(isGnawing,state);
    }

    private static final RawAnimation GNAW_ANIM = RawAnimation.begin().thenPlay("misc.gnaw");
    protected <E extends BeaverEntity> PlayState gnawAnimController(final AnimationState<E> event) {
        if (this.entityData.get(isGnawing)) {
            return event.setAndContinue(GNAW_ANIM);
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkIdleController(this),
                DefaultAnimations.genericAttackAnimation(this, DefaultAnimations.ATTACK_STRIKE),
                new AnimationController<>(this,"Gnawing",0,this::gnawAnimController)
        );

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static class BeaverDestroySapling extends Goal {
        public BeaverDestroySapling(BeaverEntity beaver, TagKey<Block> blockToDestroy, double speedModifier) {
            this.beaver = beaver;
            this.blockToDestroy = blockToDestroy;
            this.speedModifier = speedModifier;
        }

        protected final BeaverEntity beaver;
        protected final TagKey<Block> blockToDestroy;
        protected final double speedModifier;
        protected BlockPos targetPos;
        protected int idleTickCounter;
        protected int coolDownCounter;
        protected int ticksSinceReachedGoal;


        @Override
        public boolean canUse() {
            if (this.beaver.isBaby() || !net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.beaver.level(), this.beaver))
                return false;
            else if (coolDownCounter > 0) {
                coolDownCounter--;
                return false;
            }
            targetPos = getTargetPosition(beaver.level(), beaver.blockPosition());
            return targetPos != null;
        }

        protected BlockPos getTargetPosition(Level level, BlockPos pos) {
            for (int x = 0; x <= 48; x++) {
                for (int z = 0; z <= 48; z++) {
                    for (int y = 0; y <= 6; y++) {
                        BlockPos current = pos.offset(
                                x % 2 == 0 ? x / 2 : (-x - 1) / 2,
                                y % 2 == 0 ? y / 2 : (-y - 1) / 2,
                                z % 2 == 0 ? z / 2 : (-z - 1) / 2);
                        if (isValidTarget(current, level)) return current;
                    }
                }
            }
            return null;
        }

        protected boolean isValidTarget(BlockPos pos, Level level) {
            return level.getBlockState(pos).is(blockToDestroy);
        }

        protected java.util.List<ItemStack> getLoot(String StringBlock) {
            java.util.List<ItemStack> items = new java.util.ArrayList<>();
            Block block;
            if (StringBlock.lastIndexOf('_') > 6){
                StringBlock = StringBlock.substring(6, StringBlock.lastIndexOf('_')).concat("_log");
                block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(StringBlock));
                for(double i = Math.random()*4; i < 4; ++i) {
                    assert block != null;
                    items.add(new ItemStack(block.asItem()));
                }
            }
            for(double i = Math.random()*4; i < 4; ++i) items.add(new ItemStack(Items.STICK));
            return items;
        }

        @Override
        public boolean canContinueToUse() {
            return coolDownCounter <= 0 && targetPos != null && isValidTarget(targetPos, beaver.level()) && idleTickCounter >= 200 && !beaver.isBaby();
        }

        @Override
        public void tick() {
            if (!beaver.getNavigation().isStuck()) {
                beaver.getNavigation().moveTo(this.targetPos.above().getCenter().x, this.targetPos.above().getCenter().y, this.targetPos.above().getCenter().z, speedModifier);
                idleTickCounter++;
            }
            if (beaver.getOnPos().distToCenterSqr(this.targetPos.getCenter()) <= 1) {
                ticksSinceReachedGoal++;
                breakBlock();
            }else ticksSinceReachedGoal = 0;
        }

        public void breakBlock() {
            if (ticksSinceReachedGoal > 0) {
                beaver.setGnawAnim(true);
                ((ServerLevel)beaver.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, beaver.level().getBlockState(targetPos)), (double)targetPos.getX() + 0.5D, (double)targetPos.getY() + 0.7D, (double)targetPos.getZ() + 0.5D, 3, ((double)beaver.getRandom().nextFloat() - 0.5D) * 0.08D, ((double)beaver.getRandom().nextFloat() - 0.5D) * 0.08D, ((double)beaver.getRandom().nextFloat() - 0.5D) * 0.08D, 0.15D);
            }
            if (ticksSinceReachedGoal > 40){
                getLoot(beaver.level().getBlockState(targetPos).getBlock().toString()).forEach(s -> beaver.level().addFreshEntity(new ItemEntity(beaver.level(), beaver.getX(), beaver.getY(1.0D), beaver.getZ(), s)));
                beaver.level().destroyBlock(targetPos, false);

                coolDownCounter = (int) (Math.random()*1200 + 600);
                beaver.setGnawAnim(false);
                ticksSinceReachedGoal = 0;
            }
        }

        @Override
        public void stop() {
            idleTickCounter = 0;
            beaver.getNavigation().stop();
            beaver.setGnawAnim(false);

        }
    }
}
