package starlight_lnk.camerainertia;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import starlight_lnk.camerainertia.config.CameraInertiaConfigScreen;
import starlight_lnk.camerainertia.config.ClientConfig;

@Mod(CameraInertia.MODID)
public final class CameraInertia {
    public static final String MODID = "camera_inertia";

    public CameraInertia(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (modContainer, previousScreen) -> new CameraInertiaConfigScreen(previousScreen)
        );
    }
}
