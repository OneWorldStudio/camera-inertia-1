package starlight_lnk.camerainertia.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import starlight_lnk.camerainertia.CameraInertia;

@EventBusSubscriber(modid = CameraInertia.MODID, value = Dist.CLIENT)
public final class CameraFirstPersonBody {
    public static boolean isRenderingBody = false;
    public static boolean hideArms = false;

    private CameraFirstPersonBody() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        isRenderingBody = false;
        hideArms = false;
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event) {
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post<?> event) {
    }
}
