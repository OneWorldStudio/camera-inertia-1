package starlight_lnk.camerainertia.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import starlight_lnk.camerainertia.CameraInertia;

public abstract class BaseConfigScreen extends Screen {
    protected final Screen previousScreen;

    private static final Identifier LOGO_TEXTURE = Identifier.fromNamespaceAndPath(CameraInertia.MODID, "textures/gui/logo.png");

    protected BaseConfigScreen(Component title, Screen previousScreen) {
        super(title);
        this.previousScreen = previousScreen;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        // РџР РћРџРћР Р¦РР Р РћРўРЎРўРЈРџР« Р›РћР“РћРўРРџРђ
        int logoWidth = 150;
        int logoHeight = 50; // РРґРµР°Р»СЊРЅР°СЏ РїСЂРѕРїРѕСЂС†РёСЏ РґР»СЏ 370x123
        int logoX = (this.width - logoWidth) / 2;
        int logoY = 10;      // РћС‚СЃС‚СѓРї РѕС‚ РІРµСЂС…РЅРµРіРѕ РєСЂР°СЏ СЌРєСЂР°РЅР°

        graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO_TEXTURE, logoX, logoY, 0.0F, 0.0F, logoWidth, logoHeight, logoWidth, logoHeight);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        ClientConfig.SPEC.save();
        this.minecraft.setScreen(this.previousScreen);
    }

    protected void addBackButton() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> this.minecraft.setScreen(this.previousScreen))
                .bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    protected Component getToggleText(String key, boolean state) {
        return Component.translatable(key).append(": ").append(
                state ? Component.translatable("options.on") : Component.translatable("options.off")
        );
    }

    public class ConfigSlider extends AbstractSliderButton {
        private final ModConfigSpec.DoubleValue configValue;
        private final String translationKey;
        private final double min, max;

        public ConfigSlider(int x, int y, int w, int h, ModConfigSpec.DoubleValue configValue, double min, double max, String translationKey) {
            super(x, y, w, h, Component.empty(), 0.0);
            this.configValue = configValue;
            this.translationKey = translationKey;
            this.min = min;
            this.max = max;
            this.value = Mth.clamp((configValue.get() - min) / (max - min), 0.0, 1.0);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            double realValue = min + (max - min) * this.value;
            realValue = Math.round(realValue * 10.0) / 10.0;

            if (max > 10.0) {
                this.setMessage(Component.translatable(this.translationKey).append(": " + (int) realValue));
            } else {
                this.setMessage(Component.translatable(this.translationKey).append(String.format(": %.1f", realValue)));
            }
        }

        @Override
        protected void applyValue() {
            double realValue = min + (max - min) * this.value;
            realValue = Math.round(realValue * 10.0) / 10.0;
            this.configValue.set(realValue);
            ClientConfig.ACTIVE_PRESET.set(ClientConfig.Preset.CUSTOM);
        }
    }
}
