package starlight_lnk.camerainertia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import starlight_lnk.camerainertia.config.ClientConfig;

public final class CameraTurnBlur {
    private static boolean initialized = false;
    private static float lastYaw = 0.0f;

    private static float currentIntensity = 0.0f;
    private static float blurDirection = 0.0f;
    private static float fallIntensity = 0.0f;
    private static float vehicleIntensity = 0.0f;

    private static boolean blurRenderedThisFrame = false;

    private CameraTurnBlur() {
    }

    public static void reset() {
        resetStateOnly();
        blurRenderedThisFrame = false;
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || mc.isPaused()) {
            resetStateOnly();
            return;
        }

        if (!CameraViewUtils.isFirstPerson()) {
            resetStateOnly();
            return;
        }

        if (!ClientConfig.ENABLED.get() || !ClientConfig.MOTION_BLUR_ENABLED.get()) {
            resetStateOnly();
            return;
        }

        float yaw = player.getYRot();

        if (!initialized) {
            initialized = true;
            lastYaw = yaw;
            currentIntensity = 0.0f;
            blurDirection = 0.0f;
            fallIntensity = 0.0f;
            vehicleIntensity = 0.0f;
            return;
        }

        float deltaYaw = wrapDegrees(yaw - lastYaw);
        lastYaw = yaw;

        float absYaw = Math.abs(deltaYaw);

        float sensitivity = ClientConfig.MOTION_BLUR_SENSITIVITY.get().floatValue();
        float maxIntensity = Mth.clamp(ClientConfig.MOTION_BLUR_MAX_INTENSITY.get().floatValue(), 0.0f, 1.0f);
        float smoothing = Mth.clamp(ClientConfig.MOTION_BLUR_SMOOTHING.get().floatValue(), 0.01f, 1.0f);

        float deadzone = Math.max(0.0f, ClientConfig.MOTION_BLUR_DEADZONE.get().floatValue());
        float fullBlurMotion = Math.max(deadzone + 0.001f, ClientConfig.MOTION_BLUR_FULL_MOTION.get().floatValue());
        float curvePower = Math.max(0.1f, ClientConfig.MOTION_BLUR_CURVE_POWER.get().floatValue());

        float motion = absYaw * sensitivity;
        float targetIntensity = 0.0f;

        if (motion > deadzone) {
            float t = (motion - deadzone) / (fullBlurMotion - deadzone);
            t = Mth.clamp(t, 0.0f, 1.0f);

            targetIntensity = (float) Math.pow(t, curvePower) * maxIntensity;

            float turnDirection = Math.signum(deltaYaw);
            if (Math.abs(turnDirection) > 0.001f) {
                blurDirection = ClientConfig.MOTION_BLUR_OPPOSITE_SIDE.get() ? -turnDirection : turnDirection;
            }
        }

        float attackMultiplier = ClientConfig.MOTION_BLUR_ATTACK_MULTIPLIER.get().floatValue();
        float releaseMultiplier = ClientConfig.MOTION_BLUR_RELEASE_MULTIPLIER.get().floatValue();

        float attackSmoothing = Mth.clamp(smoothing * attackMultiplier, 0.01f, 1.0f);
        float releaseSmoothing = Mth.clamp(smoothing * releaseMultiplier, 0.01f, 1.0f);

        float usedSmoothing = targetIntensity > currentIntensity ? attackSmoothing : releaseSmoothing;
        currentIntensity += (targetIntensity - currentIntensity) * usedSmoothing;

        if (currentIntensity < 0.001f) {
            currentIntensity = 0.0f;
            blurDirection = 0.0f;
        }

        updateFallChannel(maxIntensity);
        updateVehicleChannel(maxIntensity);
    }

    private static void updateFallChannel(float maxIntensity) {
        if (!ClientConfig.FALL_BLUR_ENABLED.get()) {
            fallIntensity = 0.0f;
            return;
        }

        float shakeI = CameraFallShakeController.getIntensity(1.0F);
        shakeI = Mth.clamp(shakeI, 0.0f, 1.0f);

        float ratio = ClientConfig.FALL_BLUR_STRENGTH.get().floatValue();
        float target = shakeI * maxIntensity * ratio;

        float k = target > fallIntensity ? 0.10f : 0.06f;
        fallIntensity += (target - fallIntensity) * k;

        if (fallIntensity < 0.001f) {
            fallIntensity = 0.0f;
        }
    }

    private static void updateVehicleChannel(float maxIntensity) {
        float vehI = CameraVehicleSpeedFx.getBlurIntensity(1.0F);
        vehI = Mth.clamp(vehI, 0.0f, 1.0f);

        float target = vehI * maxIntensity;
        float k = target > vehicleIntensity ? 0.12f : 0.08f;
        vehicleIntensity += (target - vehicleIntensity) * k;

        if (vehicleIntensity < 0.001f) {
            vehicleIntensity = 0.0f;
        }
    }

    private static void resetStateOnly() {
        initialized = false;
        lastYaw = 0.0f;
        currentIntensity = 0.0f;
        blurDirection = 0.0f;
        fallIntensity = 0.0f;
        vehicleIntensity = 0.0f;
    }

    public static void beginFrame() {
        blurRenderedThisFrame = false;
    }

    public static void beforeGuiRender() {
        blurRenderedThisFrame = false;
    }

    public static void renderAfterHand() {
        blurRenderedThisFrame = true;
    }

    private static float wrapDegrees(float value) {
        value = value % 360.0f;

        if (value >= 180.0f) {
            value -= 360.0f;
        }

        if (value < -180.0f) {
            value += 360.0f;
        }

        return value;
    }
}
