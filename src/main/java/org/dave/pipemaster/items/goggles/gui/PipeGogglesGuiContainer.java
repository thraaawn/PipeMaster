package org.dave.pipemaster.items.goggles.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.dave.pipemaster.PipeMaster;
import org.dave.pipemaster.data.blockgroups.BlockGroup;
import org.dave.pipemaster.data.config.ConfigurationHandler;
import org.dave.pipemaster.gui.GUI;
import org.dave.pipemaster.gui.WidgetGuiContainer;
import org.dave.pipemaster.gui.event.ValueChangedEvent;
import org.dave.pipemaster.gui.event.WidgetEventResult;
import org.dave.pipemaster.gui.widgets.WidgetSelectButton;
import org.dave.pipemaster.items.goggles.PipeGogglesConfigOptions;
import org.dave.pipemaster.items.goggles.PipeGogglesData;
import org.dave.pipemaster.items.goggles.PipeGogglesItem;
import org.dave.pipemaster.items.goggles.network.PipeGogglesUpdateBlockGroupMessage;
import org.dave.pipemaster.items.goggles.network.PipeGogglesUpdateRangeMessage;
import org.dave.pipemaster.network.PipeMasterNetworkHandler;

public class PipeGogglesGuiContainer extends WidgetGuiContainer {
    private PipeGogglesData gogglesData;
    private static final ResourceLocation background = new ResourceLocation(PipeMaster.MODID, "textures/gui/pipe_goggles.png");
    public static final int WIDTH = 176;
    public static final int HEIGHT = 183;

    private final int currentItemSlot;

    public PipeGogglesGuiContainer(Container container, PipeGogglesData gogglesData, int currentItemSlot) {
        super(container);

        this.xSize = WIDTH;
        this.ySize = HEIGHT;
        this.gogglesData = gogglesData;
        this.currentItemSlot = currentItemSlot;
        this.gui = instantiateGui();
    }

    protected GUI instantiateGui() {
        GUI gui = new GUI(0, 0, WIDTH, HEIGHT);
        gui.setId("gui");

        int yPadding = 3;
        for (int channelId = 0; channelId < 4; channelId++) {
            WidgetPipeGogglesBlockGroup bgWidget = new WidgetPipeGogglesBlockGroup(ConfigurationHandler.hex2Rgb(PipeGogglesConfigOptions.optimizerColorsHex[channelId]), gogglesData.getBlockGroupForSlot(channelId));
            bgWidget.setX(5);
            bgWidget.setY(5 + (channelId * (bgWidget.height + yPadding)));

            int finalChannelId = channelId;
            bgWidget.addListener(ValueChangedEvent.class, (event, widget) -> {
                PipeMasterNetworkHandler.instance.sendToServer(new PipeGogglesUpdateBlockGroupMessage(this.currentItemSlot, finalChannelId, ((BlockGroup)event.newValue).getId()));
                return WidgetEventResult.HANDLED;
            });
            gui.add(bgWidget);
        }
        // TODO: Ensure a block group is only selectable once

        WidgetSelectButton<Integer> rangeSelectButton = new WidgetSelectButton<>();
        rangeSelectButton.setWidth(25);
        rangeSelectButton.addChoice(4, 8, 16, 32);
        rangeSelectButton.setX(145);
        rangeSelectButton.setY(7);
        rangeSelectButton.addListener(ValueChangedEvent.class, (event, widget) -> {
            PipeMasterNetworkHandler.instance.sendToServer(new PipeGogglesUpdateRangeMessage(this.currentItemSlot, (Integer)event.newValue));
            return WidgetEventResult.HANDLED;
        });
        rangeSelectButton.setValue(gogglesData.getRange());
        rangeSelectButton.setTooltipLines(I18n.format("pipemaster.pipegoggles.gui.range"));

        gui.add(rangeSelectButton);

        return gui;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
