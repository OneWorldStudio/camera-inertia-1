package starlight_lnk.camerainertia.client.mixin;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import starlight_lnk.camerainertia.client.CameraFirstPersonBody;

@Mixin(value = PlayerModel.class, remap = false)
public abstract class PlayerModelMixin {

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    @Final
    public ModelPart hat;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftSleeve;

    @Shadow
    @Final
    public ModelPart rightSleeve;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"), remap = false)
    private void cameraInertia$hideFirstPersonBodyParts(AvatarRenderState state, CallbackInfo ci) {
        if (!CameraFirstPersonBody.isRenderingBody) {
            return;
        }

        this.head.visible = false;
        this.hat.visible = false;

        if (CameraFirstPersonBody.hideArms) {
            this.leftArm.visible = false;
            this.rightArm.visible = false;
            this.leftSleeve.visible = false;
            this.rightSleeve.visible = false;
        }
    }
}
