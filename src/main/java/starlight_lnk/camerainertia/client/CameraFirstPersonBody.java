package starlight_lnk.camerainertia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import starlight_lnk.camerainertia.CameraInertia;
import starlight_lnk.camerainertia.config.ClientConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = CameraInertia.MODID, value = Dist.CLIENT)
public final class CameraFirstPersonBody {
    public static boolean isRenderingBody = false;
    public static boolean hideArms = false;

    private static final Map<Item, Boolean> TACZ_ADDON_CACHE = new ConcurrentHashMap<>();

    private CameraFirstPersonBody() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!ClientConfig.ENABLED.get() || !ClientConfig.FIRST_PERSON_BODY_ENABLED.get()) {
            return;
        }

        if (!CameraViewUtils.isFirstPerson()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (isHoldingBlacklistedItem(mc.player)) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        if (!ClientConfig.ENABLED.get() || !ClientConfig.FIRST_PERSON_BODY_ENABLED.get()) {
            return;
        }

        if (!CameraViewUtils.isFirstPerson()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        isRenderingBody = true;
        hideArms = isHoldingBlacklistedItem(player);

        ItemStack savedMain = player.getMainHandItem();
        ItemStack savedOff = player.getOffhandItem();

        try {
            if (hideArms) {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }

            float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            AvatarRenderState renderState = (AvatarRenderState) mc.getEntityRenderDispatcher().extractEntity(player, partialTick);
            renderState.showHat = false;
            if (hideArms) {
                renderState.showLeftSleeve = false;
                renderState.showRightSleeve = false;
            }

            Camera camera = mc.gameRenderer.getMainCamera();
            Vec3 cameraPos = camera.position();
            double renderX = Mth.lerp(partialTick, player.xo, player.getX()) - cameraPos.x;
            double renderY = Mth.lerp(partialTick, player.yo, player.getY()) - cameraPos.y;
            double renderZ = Mth.lerp(partialTick, player.zo, player.getZ()) - cameraPos.z;

            CameraRenderState cameraState = mc.gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;
            SubmitNodeCollector submitNodeCollector = mc.gameRenderer.getSubmitNodeStorage();
            PoseStack poseStack = event.getPoseStack();

            mc.getEntityRenderDispatcher().submit(renderState, cameraState, renderX, renderY, renderZ, poseStack, submitNodeCollector);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, savedMain);
            player.setItemInHand(InteractionHand.OFF_HAND, savedOff);
            isRenderingBody = false;
            hideArms = false;
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event) {
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post<?> event) {
    }

    private static boolean isHoldingBlacklistedItem(LocalPlayer player) {
        if (player == null) {
            return false;
        }

        return isItemHidden(player.getMainHandItem()) || isItemHidden(player.getOffhandItem());
    }

    private static boolean isItemHidden(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        var key = BuiltInRegistries.ITEM.getKey(item);
        String regName = key == null ? null : key.toString();

        if (regName != null) {
            List<? extends String> blacklist = ClientConfig.ITEM_BLACKLIST.get();
            if (blacklist != null && blacklist.contains(regName)) {
                return true;
            }
        }

        return isUniversalTacZWeapon(item, regName);
    }

    private static boolean isUniversalTacZWeapon(Item item, String regName) {
        return TACZ_ADDON_CACHE.computeIfAbsent(item, ignored -> {
            if (regName != null) {
                int colon = regName.indexOf(':');
                if (colon > 0) {
                    String namespace = regName.substring(0, colon).toLowerCase();
                    if (namespace.equals("tacz") || namespace.equals("lrtactical") || namespace.equals("lr_tactical") || namespace.equals("lesraisins")) {
                        return true;
                    }

                    String path = regName.substring(colon + 1);
                    if (path.contains("whip") || path.contains("lasso")) {
                        return true;
                    }
                }
            }

            Class<?> clazz = item.getClass();
            for (Class<?> iface : clazz.getInterfaces()) {
                String name = iface.getName().toLowerCase();
                if (name.contains("tacz") || name.contains("lrtactical") || name.contains("lr_tactical")) {
                    return true;
                }
            }

            while (clazz != null && clazz != Object.class) {
                String name = clazz.getName().toLowerCase();
                if (name.contains("tacz") || name.contains("lrtactical") || name.contains("lr_tactical")) {
                    return true;
                }
                clazz = clazz.getSuperclass();
            }

            return false;
        });
    }
}
