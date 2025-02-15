package com.plusls.carpet.mixin.rule.useDyeOnShulkerBox;

import com.plusls.carpet.PluslsCarpetAdditionSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.compat.minecraft.world.level.state.BlockStateCompat;

//#if MC > 12004
//$$ import com.plusls.carpet.mixin.accessor.AccessorBaseContainerBlockEntity;
//#endif

//#if MC < 11800
import net.minecraft.nbt.CompoundTag;
//#endif

@Mixin(DyeItem.class)
public abstract class MixinDyeItem extends Item {
    public MixinDyeItem(Properties settings) {
        super(settings);
    }

    @Shadow
    public abstract DyeColor getDyeColor();

    @Override
    @Intrinsic
    public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
        return super.useOn(useOnContext);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference", "target"})
    @Inject(
            method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void preUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!PluslsCarpetAdditionSettings.useDyeOnShulkerBox) {
            return;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);
        BlockStateCompat blockStateCompat = BlockStateCompat.of(blockState);

        if (blockStateCompat.is(Blocks.SHULKER_BOX)) {
            return;
        }

        if (!level.isClientSide()) {
            ShulkerBoxBlockEntity blockEntity = (ShulkerBoxBlockEntity) level.getBlockEntity(pos);
            BlockState newBlockState = ShulkerBoxBlock.getBlockByColor(this.getDyeColor()).defaultBlockState().
                    setValue(ShulkerBoxBlock.FACING, blockState.getValue(ShulkerBoxBlock.FACING));

            if (level.setBlockAndUpdate(pos, newBlockState)) {
                ShulkerBoxBlockEntity newBlockEntity = (ShulkerBoxBlockEntity) level.getBlockEntity(pos);
                assert blockEntity != null;
                assert newBlockEntity != null;
                newBlockEntity.loadFromTag(
                        //#if MC > 11701
                        //$$ blockEntity.saveWithoutMetadata(
                        //#if MC > 12004
                        //$$         level.registryAccess()
                        //#endif
                        //$$ )
                        //#else
                        new CompoundTag()
                        //#endif
                        //#if MC > 12004
                        //$$ , level.registryAccess()
                        //#endif
                );
                //#if MC > 12004
                //$$ ((AccessorBaseContainerBlockEntity) newBlockEntity).pca$setName(blockEntity.getCustomName());
                //#else
                newBlockEntity.setCustomName(blockEntity.getCustomName());
                //#endif
                newBlockEntity.setChanged();
                context.getItemInHand().shrink(1);
            }
        }

        cir.setReturnValue(
                //#if MC > 11502
                InteractionResult.sidedSuccess(level.isClientSide)
                //#else
                //$$ level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS
                //#endif
        );
    }
}
