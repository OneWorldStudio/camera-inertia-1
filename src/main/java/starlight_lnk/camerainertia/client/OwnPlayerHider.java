package starlight_lnk.camerainertia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import starlight_lnk.camerainertia.CameraInertia;

@EventBusSubscriber(modid = CameraInertia.MODID, value = Dist.CLIENT)
public final class OwnPlayerHider {
    private OwnPlayerHider() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event) {
        if (!CameraPerspectiveController.shouldHideOwnPlayer()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = mc.player;
        if (self == null) {
            return;
        }
        if (event.getRenderState().id != self.getId()) {
            return;
        }

        float partial = event.getPartialTick();
        if (CameraPerspectiveController.shouldFullyHideOwnPlayer(partial)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post<?> event) {
    }
}
