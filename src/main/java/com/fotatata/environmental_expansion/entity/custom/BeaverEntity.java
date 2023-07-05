package com.fotatata.environmental_expansion.entity.custom;

import com.fotatata.environmental_expansion.entity.ModEntityTypes;
import com.mojang.datafixers.types.templates.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraftforge.common.Tags;
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
    private static final Ingredient FOOD_ITEMS = Ingredient.of(ItemTags.SAPLINGS);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1,new FloatGoal(this));
        this.goalSelector.addGoal(1,new PanicGoal(this,0.8d));
        this.goalSelector.addGoal(2, new BreedGoal(this,0.5d));
        this.goalSelector.addGoal(2, new TemptGoal(this,0.6d, FOOD_ITEMS,false));
        this.goalSelector.addGoal(3,new FollowParentGoal(this, 0.6d));
        this.goalSelector.addGoal(4, new BeaverDestroySapling(this, BlockTags.SAPLINGS,0.5d));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this,0.4d));
        this.goalSelector.addGoal(5,new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6,new LookAtPlayerGoal(this, Player.class,6.0f));
    }

    public class BeaverDestroySapling extends Goal {
        public BeaverDestroySapling(PathfinderMob mob, TagKey<Block> blocksToDestroy, double speedModifier) {
            this.mob = mob;
            this.blocksToDestroy = blocksToDestroy;
            this.speedModifier = speedModifier;
        }

        protected final PathfinderMob mob;
        protected final TagKey<Block> blocksToDestroy;
        protected final double speedModifier;
        protected BlockPos targetPos;
        protected int idleTickCounter;
        protected int cooldownCounter;


        @Override
        public boolean canUse() {
            if(cooldownCounter > 0) {
                cooldownCounter--;
                return false;
            }
            targetPos = generateTarget(mob.level(),mob.blockPosition(),24, 3);
            return targetPos != null;
        }

        protected BlockPos generateTarget(Level level,BlockPos pos, int range, int verticalRange){
            for(int x = -range; x <= range;x++){
                for(int z = -range; z <= range;z++){
                    for(int y = -range; y <= verticalRange;y++){
                        BlockPos current = pos.offset(x,y,z);
                        if(isValidTarget(current, level))
                            return current;
                    }
                }
            }
            return null;
        }

        protected boolean isValidTarget(BlockPos pos, Level level){
            return level.getBlockState(pos).is(blocksToDestroy);
        }

        @Override
        public boolean canContinueToUse() {
            return cooldownCounter <= 0 && targetPos != null && isValidTarget(targetPos, mob.level()) && idleTickCounter >= 200;
        }

        @Override
        public void tick() {
            if(!mob.getNavigation().isStuck()){
                mob.getNavigation().moveTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), speedModifier);
            }else this.idleTickCounter++;
            if(mob.blockPosition().distToCenterSqr(this.targetPos.getX(),this.targetPos.getY(),this.targetPos.getZ()) <= 1){
                breakBlock();
            }
        }

        public void breakBlock(){
            this.mob.level().destroyBlock(targetPos, true);
            this.cooldownCounter = 600;
        }

        @Override
        public void stop() {
            this.idleTickCounter= 0;
            this.mob.getNavigation().stop();
        }

        @Override
        public void start() {mob.getNavigation().moveTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), speedModifier);}
    }

    public BeaverEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier setAttributes(){
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH,20d)
                .add(Attributes.MOVEMENT_SPEED, 0.4d).build();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel,@Nullable AgeableMob ageableMob) {
        return ModEntityTypes.BEAVER.get().create(serverLevel);
    }

    public boolean isFood(@NotNull ItemStack p_28271_) {return FOOD_ITEMS.test(p_28271_);}

    public boolean isGnawing = false;

    private static final RawAnimation GNAW_ANIM = RawAnimation.begin().thenPlay("misc.gnaw");
    protected <E extends BeaverEntity> PlayState gnawAnimController(final AnimationState<E> event) {
        if (isGnawing)
            return event.setAndContinue(GNAW_ANIM);

        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkIdleController(this),
                DefaultAnimations.genericAttackAnimation(this, DefaultAnimations.ATTACK_STRIKE),
                new AnimationController<>(this,"Gnawing",5,this::gnawAnimController)
        );

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
