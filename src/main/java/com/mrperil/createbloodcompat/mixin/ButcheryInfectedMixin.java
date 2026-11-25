package com.mrperil.createbloodcompat.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.mcreator.butchery.init.ButcheryModFluids;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ver.faker.nocturnal.content.vampire_piston.VampirePistonBlockEntity;

import java.util.function.Predicate;

@Mixin(VampirePistonBlockEntity.class)
public class ButcheryInfectedMixin {

    @Shadow
    protected SmartFluidTank tankInventory;

    @Unique
    private static boolean createbloodcompat$isInfectedFuel(FluidStack stack) {
        if (stack.isEmpty()) return false;
        Fluid targetFluid = ButcheryModFluids.INFECTED_BLOOD.get();
        return targetFluid != Fluids.EMPTY && stack.getFluid() == targetFluid;
    }

    @Unique
    private boolean createbloodcompat$hasInfectedFuel() {
        return this.tankInventory != null && createbloodcompat$isInfectedFuel(this.tankInventory.getFluid());
    }

    // 1. Allow the imported mod's fluid into tankInventory
    @ModifyArg(
            method = "addBehaviours",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/fluid/SmartFluidTank;setValidator(Ljava/util/function/Predicate;)Lnet/neoforged/neoforge/fluids/capability/templates/FluidTank;"
            ),
            index = 0
    )
    private Predicate<FluidStack> createbloodcompat$allowInfectedFuel(Predicate<FluidStack> originalValidator) {
        return fs -> originalValidator.test(fs) || createbloodcompat$isInfectedFuel(fs);
    }

    // 2. Increase RPM when running on the alternative fuel (Base is 32.0F)
    @ModifyReturnValue(method = "getGeneratedSpeed", at = @At("RETURN"))
    private float createbloodcompat$boostSpeed(float originalSpeed) {
        if (originalSpeed != 0.0F && createbloodcompat$hasInfectedFuel()) {
            return originalSpeed * 0.66F; // Example: 32 RPM -> 48 RPM
        }
        return originalSpeed;
    }

    // 3. Increase Stress Capacity per RPM when running on the alternative fuel (Base is 256.0F * height)
    @ModifyReturnValue(method = "calculateAddedStressCapacity", at = @At("RETURN"))
    private float createbloodcompat$boostStressCapacity(float originalCapacity) {
        if (createbloodcompat$hasInfectedFuel()) {
            return originalCapacity * 0.5F; // Example: 256 -> 512 SU/RPM
        }
        return originalCapacity;
    }
}