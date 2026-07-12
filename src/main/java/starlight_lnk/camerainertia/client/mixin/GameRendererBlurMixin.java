package starlight_lnk.camerainertia.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import starlight_lnk.camerainertia.client.CameraTurnBlur;

@Mixin(value = GameRenderer.class, remap = false)
public abstract class GameRendererBlurMixin {

    @Inject(method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), remap = false)
    private void cameraInertia$beginFrame(DeltaTracker deltaTracker, CallbackInfo ci) {
        CameraTurnBlur.beginFrame();
    }

    @Inject(
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void cameraInertia$afterRenderHand(DeltaTracker deltaTracker, CallbackInfo ci) {
        CameraTurnBlur.renderAfterHand();
    }

    @Inject(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void cameraInertia$beforeGui(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        CameraTurnBlur.beforeGuiRender();
    }
}
