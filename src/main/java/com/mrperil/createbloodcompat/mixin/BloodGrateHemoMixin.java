package com.mrperil.createbloodcompat.mixin;


import net.mcreator.butchery.block.entity.BloodgrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BloodgrateBlockEntity.class)
public class BloodGrateHemoMixin {

    @Shadow
    @Final
    @Mutable
    private FluidTank fluidTank;

    @Unique
    private static final ResourceLocation BUTCHERY_BLOOD = ResourceLocation.fromNamespaceAndPath("butchery", "blood");
    @Unique
    private static final ResourceLocation HEMO_BLOOD = ResourceLocation.fromNamespaceAndPath("hemodynamics", "blood");

    @Inject(method = "<init>", at = @At("RETURN"))
    private void replaceFluidTank(BlockPos position, BlockState state, CallbackInfo ci) {
        final BlockEntity self = (BlockEntity) (Object) this;

        this.fluidTank = new FluidTank(16000) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                self.setChanged();
                if (self.getLevel() != null) {
                    BlockPos pos = self.getBlockPos();
                    BlockState blockState = self.getLevel().getBlockState(pos);
                    self.getLevel().sendBlockUpdated(pos, blockState, blockState, 2);
                }
            }

            @Override
            public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
                if (resource != null && !resource.isEmpty()
                        && BuiltInRegistries.FLUID.getKey(resource.getFluid()).equals(BUTCHERY_BLOOD)) {
                    Fluid hemoFluid = BuiltInRegistries.FLUID.get(HEMO_BLOOD);
                    if (hemoFluid != Fluids.EMPTY) {
                        return super.fill(new FluidStack(hemoFluid, resource.getAmount()), action);
                    }
                }
                return super.fill(resource, action);
            }
        };
    }

}