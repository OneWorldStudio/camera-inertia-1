package starlight_lnk.camerainertia.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import starlight_lnk.camerainertia.client.CameraFirstPersonBody;

@Mixin(value = CustomHeadLayer.class, remap = false)
public abstract class CustomHeadLayerMixin {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true, remap = false)
    private void cameraInertia$hidePumpkinsAndSkulls(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            LivingEntityRenderState state,
            float yRot,
            float xRot,
            CallbackInfo ci
    ) {
        if (CameraFirstPersonBody.isRenderingBody) {
            ci.cancel();
        }
    }
}
