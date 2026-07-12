package starlight_lnk.camerainertia.client;

import starlight_lnk.camerainertia.CameraInertia;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(
        modid = CameraInertia.MODID,
        value = Dist.CLIENT
)
public final class ClientForgeEvents {

    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        try {
            // РўРёРєР°РµРј РІСЃРµ РЅР°С€Рё РєСЂСѓС‚С‹Рµ СЌС„С„РµРєС‚С‹ 1-РіРѕ Р»РёС†Р°
            CameraPerspectiveController.tick();
            CameraTurnBlur.onClientTick();
            CameraRollController.tick();
            CameraPitchController.tick();
            CameraFovController.tick();
            CameraDamageController.tick();
            CameraMovementController.tick();
            CameraCombatController.tick();
            CameraEffectsController.tick();
            CameraVehicleController.tick();
            CameraWalkController.tick();
            CameraVehicleSpeedFx.tick();
            CameraMiningController.tick();
            CameraTridentZoomController.tick();
            CameraFallShakeController.tick();
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        try {
            if (!CameraViewUtils.isFirstPerson()) return;
            float multiplier = CameraFovController.getFovMultiplier(1.0F);
            multiplier *= CameraTridentZoomController.getFovMultiplier(1.0F);
            event.setNewFovModifier(event.getNewFovModifier() * multiplier);
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        try {
            // Р•СЃР»Рё РјС‹ РѕС‚ 3-РіРѕ Р»РёС†Р°, РјРѕРґ Р’РћРћР‘Р©Р• РЅРёС‡РµРіРѕ РЅРµ РґРµР»Р°РµС‚! Р’Р°РЅРёР»Р° СЂР°Р±РѕС‚Р°РµС‚ РёРґРµР°Р»СЊРЅРѕ.
            if (!CameraViewUtils.isFirstPerson()) return;

            float partial = (float) event.getPartialTick();
            float pedestrianMul = CameraVehicleController.getPedestrianMultiplier();

            // рџЋҐ РЎРєР»Р°РґС‹РІР°РµРј РІСЃРµ СЌС„С„РµРєС‚С‹ РёРЅРµСЂС†РёРё РґР»СЏ 1-Р“Рћ Р›РР¦Рђ
            float pitchKick =
                    CameraDamageController.getPitchOffset(partial)
                            + CameraMovementController.getPitchOffset(partial) * pedestrianMul
                            + CameraPitchController.getPitch(partial)
                            + CameraCombatController.getPitchOffset(partial)
                            + CameraEffectsController.getPitchOffset(partial)
                            + CameraVehicleController.getPitchOffset(partial)
                            + CameraMiningController.getPitchOffset(partial)
                            + CameraFallShakeController.getPitchOffset(partial)
                            + CameraWalkController.getPitch(partial);

            float yawKick =
                    CameraDamageController.getYawOffset(partial)
                            + CameraMovementController.getYawOffset(partial) * pedestrianMul
                            + CameraCombatController.getYawOffset(partial)
                            + CameraEffectsController.getYawOffset(partial)
                            + CameraVehicleController.getYawOffset(partial)
                            + CameraMiningController.getYawOffset(partial)
                            + CameraFallShakeController.getYawOffset(partial)
                            + CameraWalkController.getYaw(partial);

            float rollKick =
                    CameraDamageController.getRollOffset(partial)
                            + CameraMovementController.getRollOffset(partial) * pedestrianMul
                            + CameraRollController.getRoll(partial)
                            + CameraCombatController.getRollOffset(partial)
                            + CameraEffectsController.getRollOffset(partial)
                            + CameraVehicleController.getRollOffset(partial)
                            + CameraMiningController.getRollOffset(partial)
                            + CameraFallShakeController.getRollOffset(partial)
                            + CameraWalkController.getRoll(partial);

            event.setPitch(event.getPitch() + pitchKick);
            event.setYaw(event.getYaw()     + yawKick);
            event.setRoll(event.getRoll()   + rollKick);

        } catch (Throwable ignored) {}
    }
}
