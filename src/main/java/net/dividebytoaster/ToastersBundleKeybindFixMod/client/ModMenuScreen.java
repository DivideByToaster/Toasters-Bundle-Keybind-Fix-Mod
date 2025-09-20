package net.dividebytoaster.ToastersBundleKeybindFixMod.client;

/// Imports - Minecraft
import net.dividebytoaster.ToastersBundleKeybindFixMod.ModMain;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

/// Imports - Local
import static net.dividebytoaster.ToastersBundleKeybindFixMod.client.ModMenu.CLIENT;

public class ModMenuScreen
extends Screen
{
    public static final int COLOR_DARKEN = 0x7F000000;

    protected TextWidget      titleLabel;
    protected ButtonWidget    buttonDone;
    protected ClickableWidget background;

    protected CheckboxWidget boxKeySwap;
    protected CheckboxWidget boxOnEmpty;

    public ModMenuScreen(Screen parent)
    {
        super(Text.of("Bundle Options"));
    }

    @Override
    protected void init()
    {
        titleLabel_init();
        buttonDone_init();
        settings_init();
    }

    private void titleLabel_init()
    {
        titleLabel = new TextWidget(title, CLIENT.textRenderer);

        int width = titleLabel.getWidth();
        titleLabel.setPosition((this.width - width) / 2, 12);

        addDrawableChild(titleLabel);
    }

    private void buttonDone_init()
    {
        int width = (200 + (this.width % 2));
        int height = 20;

        int x = (this.width - width) / 2;
        int y = this.height - height - 6;

        ButtonWidget.Builder builder = ButtonWidget.builder
        (
            Text.literal("Done"),
            (buttonWidget) -> close()
        );
        builder.dimensions(x, y, width, height);
        buttonDone = builder.build();
        buttonDone.setNavigationOrder(Integer.MAX_VALUE);

        addDrawableChild(buttonDone);
    }

    private void settings_init()
    {
        int bg_x = 0;
        int bg_y = titleLabel.getY() + titleLabel.getHeight() + 10;

        int bg_width = this.width;
        int bg_height = buttonDone.getY() - bg_y - 6;

        background = new ClickableWidget(-1, -1, 0, 0, Text.of(""))
        {
            @Override
            protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks)
            {
                context.fill
                (
                    bg_x,
                    bg_y,
                    bg_x + bg_width,
                    bg_y + bg_height,
                    COLOR_DARKEN
                );
            }

            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder)
            {
                // TODO
            }
        };

        boxKeySwap = CheckboxWidget.builder
        (
            Text.of("Right-click adds and removes items, left click does not."),
            CLIENT.textRenderer
        )
        .callback((box, val) -> ModMain.key_swap(val))
        .checked(ModMain.key_swap())
        .pos(bg_x + 10, bg_y + 10)
        .build();

        boxOnEmpty = CheckboxWidget.builder
        (
            Text.of("Right-click picks up and places empty bundles."),
            CLIENT.textRenderer
        )
        .callback((box, val) -> ModMain.on_empty(val))
        .checked(ModMain.on_empty())
        .pos(bg_x + 10, bg_y + 30)
        .build();

        addDrawableChild(background);
        addDrawableChild(boxKeySwap);
        addDrawableChild(boxOnEmpty);
    }
}
