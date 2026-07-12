package starlight_lnk.camerainertia.client.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import starlight_lnk.camerainertia.client.CameraFirstPersonBody;

@Mixin(value = HumanoidArmorLayer.class, remap = false)
public abstract class ArmorLayerMixin {

    @Shadow
    @Final
    private ArmorModelSet<?> modelSet;

    @Shadow
    @Final
    private ArmorModelSet<?> babyModelSet;

    @Redirect(
            method = "renderArmorPiece",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;getArmorModel(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/client/model/HumanoidModel;"
            ),
            remap = false
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private HumanoidModel cameraInertia$hideArmorArms(
            HumanoidArmorLayer self,
            HumanoidRenderState state,
            EquipmentSlot slot
    ) {
        HumanoidModel model = (HumanoidModel) (state.isBaby ? this.babyModelSet : this.modelSet).get(slot);
        if (CameraFirstPersonBody.isRenderingBody && slot == EquipmentSlot.CHEST && CameraFirstPersonBody.hideArms) {
            model.leftArm.visible = false;
            model.rightArm.visible = false;
        }
        return model;
    }

    @Inject(method = "renderArmorPiece", at = @At("RETURN"), remap = false)
    @SuppressWarnings("rawtypes")
    private void cameraInertia$restoreArms(
            HumanoidArmorLayer self,
            SubmitNodeCollector submitNodeCollector,
            net.minecraft.world.item.ItemStack itemStack,
            EquipmentSlot slot,
            int lightCoords,
            HumanoidRenderState state,
            CallbackInfo ci
    ) {
        if (CameraFirstPersonBody.isRenderingBody && slot == EquipmentSlot.CHEST && CameraFirstPersonBody.hideArms) {
            HumanoidModel model = (HumanoidModel) (state.isBaby ? this.babyModelSet : this.modelSet).get(slot);
            model.leftArm.visible = true;
            model.rightArm.visible = true;
        }
    }
}
