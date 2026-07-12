package starlight_lnk.camerainertia.client.mixin;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import starlight_lnk.camerainertia.client.CameraPerspectiveController;

@Mixin(value = Camera.class, remap = false)
public abstract class CameraF5AnimationMixin {

    @Shadow
    private Level level;

    @Shadow
    private Entity entity;

    @Shadow
    private boolean detached;

    @Shadow
    public abstract float xRot();

    @Shadow
    public abstract float yRot();

    @Shadow
    public abstract Vec3 position();

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Unique
    private float currentPartialTick;

    @Inject(method = "alignWithEntity(F)V", at = @At("HEAD"), remap = false)
    private void cameraInertia$capturePartialTick(float partialTicks, CallbackInfo ci) {
        this.currentPartialTick = partialTicks;
    }

    @ModifyArg(
            method = "alignWithEntity(F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"),
            index = 0,
            remap = false
    )
    private float cameraInertia$animateF5Distance(float originalDistance) {
        if (CameraPerspectiveController.isTransitioning()) {
            return (float) CameraPerspectiveController.getInterpolatedDistance(this.currentPartialTick);
        }
        return originalDistance;
    }

    @Inject(method = "alignWithEntity(F)V", at = @At("TAIL"), remap = false)
    private void cameraInertia$applyMirrorsEdgeOffset(float partialTicks, CallbackInfo ci) {
        if (this.level == null || this.entity == null || this.detached || this.entity.isPassenger()) {
            return;
        }

        float yaw = this.yRot();
        float pitch = this.xRot();
        float lookDownFactor = Math.max(0.0F, pitch) / 90.0F;

        double maxForward = 0.15 + (lookDownFactor * 0.25);
        double maxDown = lookDownFactor * 0.30;

        double rad = Math.toRadians(yaw);
        double dx = -Math.sin(rad) * maxForward;
        double dz = Math.cos(rad) * maxForward;

        Vec3 startPos = this.position();
        Vec3 offsetVec = new Vec3(dx, -maxDown, dz);
        double maxMoveDist = offsetVec.length();
        Vec3 moveDir = offsetVec.normalize();

        double checkDist = maxMoveDist + 0.35;
        Vec3 checkTarget = startPos.add(moveDir.scale(checkDist));

        HitResult hit = this.level.clip(
                new ClipContext(
                        startPos,
                        checkTarget,
                        ClipContext.Block.VISUAL,
                        ClipContext.Fluid.NONE,
                        this.entity
                )
        );

        double finalMoveDist = maxMoveDist;
        if (hit.getType() != HitResult.Type.MISS) {
            double hitDist = startPos.distanceTo(hit.getLocation());
            double availableSpace = hitDist - 0.30;
            finalMoveDist = availableSpace <= 0.0 ? 0.0 : Math.min(maxMoveDist, availableSpace);
        }

        if (finalMoveDist > 0.0) {
            Vec3 finalPos = startPos.add(moveDir.scale(finalMoveDist));
            this.setPosition(finalPos.x, finalPos.y, finalPos.z);
        }
    }
}
