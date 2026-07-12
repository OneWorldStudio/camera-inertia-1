package starlight_lnk.camerainertia.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GameRenderer.class, remap = false)
public abstract class CameraRollMixin {
}
