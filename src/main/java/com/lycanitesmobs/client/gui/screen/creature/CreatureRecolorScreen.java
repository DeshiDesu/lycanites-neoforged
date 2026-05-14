package com.lycanitesmobs.client.gui.screen.creature;

import com.lycanitesmobs.client.gui.screen.base.BaseGui;
import com.lycanitesmobs.client.gui.screen.base.BaseScreen;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class CreatureRecolorScreen extends BaseScreen {

    private final BaseCreatureEntity creature;

    private static final int HANDLE_OFFSET_Y = 12;


    private FloatSlider baseRSlider;
    private FloatSlider baseGSlider;
    private FloatSlider baseBSlider;
    private FloatSlider baseRangeSlider;

    private FloatSlider varRSlider;
    private FloatSlider varGSlider;
    private FloatSlider varBSlider;
    private FloatSlider amountSlider;

    private float baseR;
    private float baseG;
    private float baseB;
    private float baseRange;

    private float variantR;
    private float variantG;
    private float variantB;
    private float variantAmount;
    private RecolorLayoutConfig.Group leftGroup;
    private RecolorLayoutConfig.Group rightGroup;

    private Button confirmButton;

    private boolean draggingLeft;
    private boolean draggingRight;
    private boolean resizingLeft;
    private boolean resizingRight;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean confirmed;
    private boolean rotatingPreview;
    private double lastRotateMouseX;
    private double lastRotateMouseY;
    private float previewYaw;
    private float previewPitch;
    private float previewZoom = 1.0F;


    public CreatureRecolorScreen(BaseCreatureEntity creature) {
        super(Component.literal("Creature Recolor"));
        this.creature = creature;
    }

    @Override
    public void init() {
        if (this.creature == null || this.creature.isRemoved()) {
            this.onClose();
            return;
        }

        RecolorDebug.attachTo(this.creature);

        this.baseR = RecolorDebug.baseR;
        this.baseG = RecolorDebug.baseG;
        this.baseB = RecolorDebug.baseB;
        this.baseRange = RecolorDebug.baseRange;

        this.variantR = RecolorDebug.variantR;
        this.variantG = RecolorDebug.variantG;
        this.variantB = RecolorDebug.variantB;
        this.variantAmount = RecolorDebug.variantAmount;

        super.init();
    }


    @Override
    public void onClose() {
        super.onClose();
        if (!confirmed) {
            RecolorDebug.detach();
        }
    }


    @Override
    protected void initWidgets() {
        RecolorLayoutConfig cfg = RecolorLayoutConfig.get();
        leftGroup = cfg.left;
        rightGroup = cfg.right;

        int defaultPanelHeight = 220;
        int defaultPanelWidth = 260;
        int defaultPreviewWidth = 140;

        if (leftGroup.width <= 0) {
            leftGroup.width = defaultPanelWidth;
            leftGroup.height = defaultPanelHeight;
        }
        if (rightGroup.width <= 0) {
            rightGroup.width = defaultPreviewWidth;
            rightGroup.height = defaultPanelHeight;
        }

        int totalWidth = leftGroup.width + rightGroup.width + 10;
        int leftX = (this.width - totalWidth) / 2;
        int leftY = this.height / 2 - leftGroup.height / 2;

        if (leftGroup.x == 0 && leftGroup.y == 0) {
            leftGroup.x = leftX;
            leftGroup.y = leftY;
        }
        if (rightGroup.x == 0 && rightGroup.y == 0) {
            rightGroup.x = leftGroup.x + leftGroup.width + 10;
            rightGroup.y = leftGroup.y;
        }

        int sliderWidth = leftGroup.width;
        int sliderHeight = 20;

        baseRSlider = new FloatSlider(
                0, 0, sliderWidth, sliderHeight,
                "Base R",
                () -> baseR,
                v -> baseR = clamp01((float) v)
        );
        this.addRenderableWidget(baseRSlider);

        baseGSlider = new FloatSlider(
                0, 0, sliderWidth, sliderHeight,
                "Base G",
                () -> baseG,
                v -> baseG = clamp01((float) v)
        );
        this.addRenderableWidget(baseGSlider);

        baseBSlider = new FloatSlider(
                0, 0, sliderWidth, sliderHeight,
                "Base B",
                () -> baseB,
                v -> baseB = clamp01((float) v)
        );
        this.addRenderableWidget(baseBSlider);

        baseRangeSlider = new FloatSlider(
                0, 0, sliderWidth, sliderHeight,
                "BaseRange",
                () -> baseRange,
                v -> baseRange = clamp01((float) v)
        );
        this.addRenderableWidget(baseRangeSlider);

        varRSlider = new FloatSlider(
                0, 0, sliderWidth, sliderHeight,
                "Var R",
                () -> variantR,
                v -> variantR = clamp01((float) v)
        );
        this.addRenderableWidget(varRSlider);

        varGSlider = new FloatSlider(
                0, 0, sliderWidth, sliderHeight,
                "Var G",
                () -> variantG,
                v -> variantG = clamp01((float) v)
        );
        this.addRenderableWidget(varGSlider);

        varBSlider = new FloatSlider(
                0, 0, sliderWidth, sliderHeight,
                "Var B",
                () -> variantB,
                v -> variantB = clamp01((float) v)
        );
        this.addRenderableWidget(varBSlider);

        amountSlider = new FloatSlider(
                0, 0, sliderWidth, sliderHeight,
                "Amount",
                () -> variantAmount,
                v -> variantAmount = clamp01((float) v)
        );
        this.addRenderableWidget(amountSlider);

        confirmButton = Button.builder(Component.literal("Confirm"), b -> applyAndClose())
                .bounds(0, 0, leftGroup.width, 20)
                .build();
        this.addRenderableWidget(confirmButton);

        relayoutControls();
    }

    private void relayoutControls() {
        int sliderHeight = 20;
        int row = 22;

        int confirmHeight = 20;
        int confirmY = leftGroup.y + 4;

        confirmButton.setX(leftGroup.x);
        confirmButton.setY(confirmY);
        confirmButton.setWidth(leftGroup.width);
        confirmButton.setHeight(confirmHeight);

        int x = leftGroup.x;
        int y = confirmY + confirmHeight + 4;

        baseRSlider.setX(x);
        baseRSlider.setY(y);
        baseRSlider.setWidth(leftGroup.width);
        baseRSlider.setHeight(sliderHeight);
        y += row;

        baseGSlider.setX(x);
        baseGSlider.setY(y);
        baseGSlider.setWidth(leftGroup.width);
        baseGSlider.setHeight(sliderHeight);
        y += row;

        baseBSlider.setX(x);
        baseBSlider.setY(y);
        baseBSlider.setWidth(leftGroup.width);
        baseBSlider.setHeight(sliderHeight);
        y += row;

        baseRangeSlider.setX(x);
        baseRangeSlider.setY(y);
        baseRangeSlider.setWidth(leftGroup.width);
        baseRangeSlider.setHeight(sliderHeight);
        y += row;

        varRSlider.setX(x);
        varRSlider.setY(y);
        varRSlider.setWidth(leftGroup.width);
        varRSlider.setHeight(sliderHeight);
        y += row;

        varGSlider.setX(x);
        varGSlider.setY(y);
        varGSlider.setWidth(leftGroup.width);
        varGSlider.setHeight(sliderHeight);
        y += row;

        varBSlider.setX(x);
        varBSlider.setY(y);
        varBSlider.setWidth(leftGroup.width);
        varBSlider.setHeight(sliderHeight);
        y += row;

        amountSlider.setX(x);
        amountSlider.setY(y);
        amountSlider.setWidth(leftGroup.width);
        amountSlider.setHeight(sliderHeight);
    }

    private void applyAndClose() {
        RecolorDebug.baseR = baseR;
        RecolorDebug.baseG = baseG;
        RecolorDebug.baseB = baseB;
        RecolorDebug.baseRange = baseRange;

        RecolorDebug.variantR = variantR;
        RecolorDebug.variantG = variantG;
        RecolorDebug.variantB = variantB;
        RecolorDebug.variantAmount = variantAmount;

        confirmed = true;
        this.minecraft.setScreen(null);
    }


    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(guiGraphics);

        RenderSystem.enableBlend();
        guiGraphics.fill(leftGroup.x, leftGroup.y, leftGroup.x + leftGroup.width, leftGroup.y + leftGroup.height, 0xC0000000);

        int cardPadding = 4;
        int cardTop = rightGroup.y + 12;
        int cardBottom = rightGroup.y + rightGroup.height - 28;
        int cardLeft = rightGroup.x;
        int cardRight = rightGroup.x + rightGroup.width;

        guiGraphics.fill(cardLeft, cardTop, cardRight, cardBottom, 0xF0202020);
        guiGraphics.fill(cardLeft + 1, cardTop + 1, cardRight - 1, cardBottom - 1, 0xF0303030);

        renderResizeHandles(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.creature == null || this.creature.isRemoved()) {
            this.onClose();
            return;
        }

        var font = this.minecraft.font;

        guiGraphics.drawString(
                font,
                "Editing: " + this.creature.getName().getString(),
                leftGroup.x,
                leftGroup.y - 18,
                0xFFFFFF,
                false
        );
        int cardTop = rightGroup.y + 12;
        int cardBottom = rightGroup.y + rightGroup.height - 28;
        int cardLeft = rightGroup.x;
        int cardRight = rightGroup.x + rightGroup.width;


        float oldBaseR = RecolorDebug.baseR;
        float oldBaseG = RecolorDebug.baseG;
        float oldBaseB = RecolorDebug.baseB;
        float oldBaseRange = RecolorDebug.baseRange;
        float oldVarR = RecolorDebug.variantR;
        float oldVarG = RecolorDebug.variantG;
        float oldVarB = RecolorDebug.variantB;
        float oldVarAmt = RecolorDebug.variantAmount;

        RecolorDebug.baseR = baseR;
        RecolorDebug.baseG = baseG;
        RecolorDebug.baseB = baseB;
        RecolorDebug.baseRange = baseRange;
        RecolorDebug.variantR = variantR;
        RecolorDebug.variantG = variantG;
        RecolorDebug.variantB = variantB;
        RecolorDebug.variantAmount = variantAmount;

        int previewCenterX = (cardLeft + cardRight) / 2;
        int previewCenterY = (cardTop + cardBottom) / 2 + 10;
        double w = this.creature.getBbWidth();
        double h = this.creature.getBbHeight();
        int baseScale = (int) Math.round((2.5F / Math.max(w, h)) * 20);
        int scale = (int) (baseScale * previewZoom);

        BaseGui.renderLivingEntityRotated(
                guiGraphics,
                previewCenterX,
                previewCenterY,
                scale,
                previewYaw,
                previewPitch,
                this.creature
        );

        RecolorDebug.baseR = oldBaseR;
        RecolorDebug.baseG = oldBaseG;
        RecolorDebug.baseB = oldBaseB;
        RecolorDebug.baseRange = oldBaseRange;
        RecolorDebug.variantR = oldVarR;
        RecolorDebug.variantG = oldVarG;
        RecolorDebug.variantB = oldVarB;
        RecolorDebug.variantAmount = oldVarAmt;

    }

    @Override
    public void actionPerformed(int buttonId) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !hasShiftDown() && insidePreview(mouseX, mouseY)) {
            rotatingPreview = true;
            lastRotateMouseX = mouseX;
            lastRotateMouseY = mouseY;
            return true;
        }

        if (button == 0 && hasShiftDown()) {
            if (inside(mouseX, mouseY, leftGroup)) {
                draggingLeft = true;
                resizingLeft = nearBottomRight(mouseX, mouseY, leftGroup);
                dragOffsetX = (int) mouseX - leftGroup.x;
                dragOffsetY = (int) mouseY - leftGroup.y;
                return true;
            }
            if (inside(mouseX, mouseY, rightGroup)) {
                draggingRight = true;
                resizingRight = nearBottomRight(mouseX, mouseY, rightGroup);
                dragOffsetX = (int) mouseX - rightGroup.x;
                dragOffsetY = (int) mouseY - rightGroup.y;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (button == 0 && rotatingPreview) {
            double deltaX = mouseX - lastRotateMouseX;
            double deltaY = mouseY - lastRotateMouseY;
            lastRotateMouseX = mouseX;
            lastRotateMouseY = mouseY;

            previewYaw -= (float) deltaX * 0.75F;
            previewPitch = clampPreviewPitch(previewPitch + (float) deltaY * 0.75F);

            if (previewYaw > 360F) previewYaw -= 360F;
            if (previewYaw < -360F) previewYaw += 360F;

            return true;
        }

        if (button == 0 && hasShiftDown()) {
            if (draggingLeft) {
                if (resizingLeft) {
                    leftGroup.width = Math.max(120, (int) mouseX - leftGroup.x);
                    leftGroup.height = Math.max(80, (int) mouseY - leftGroup.y);
                } else {
                    leftGroup.x = (int) mouseX - dragOffsetX;
                    leftGroup.y = (int) mouseY - dragOffsetY;
                }
                relayoutControls();
                return true;
            }
            if (draggingRight) {
                if (resizingRight) {
                    rightGroup.width = Math.max(120, (int) mouseX - rightGroup.x);
                    rightGroup.height = Math.max(80, (int) mouseY - rightGroup.y);
                } else {
                    rightGroup.x = (int) mouseX - dragOffsetX;
                    rightGroup.y = (int) mouseY - dragOffsetY;
                }
                relayoutControls();
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            rotatingPreview = false;
            if (draggingLeft || draggingRight) {
                draggingLeft = false;
                draggingRight = false;
                resizingLeft = false;
                resizingRight = false;
                RecolorLayoutConfig.save();
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (insidePreview(mouseX, mouseY)) {
            float zoomStep = 0.1F;
            previewZoom += (float) delta * zoomStep;
            previewZoom = Mth.clamp(previewZoom, 0.3F, 2.5F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }


    private static float clampPreviewPitch(float v) {
        if (v < -80.0F) return -80.0F;
        if (v > 80.0F) return 80.0F;
        return v;
    }

    private static boolean inside(double x, double y, RecolorLayoutConfig.Group g) {
        return x >= g.x && x <= g.x + g.width && y >= g.y && y <= g.y + g.height;
    }

    private static boolean nearBottomRight(double x, double y, RecolorLayoutConfig.Group g) {
        int handleSize = 12;
        int hx = g.x + g.width - handleSize;
        int hy = g.y + g.height - handleSize - HANDLE_OFFSET_Y;
        return x >= hx && x <= g.x + g.width && y >= hy && y <= hy + handleSize;
    }

    private void renderResizeHandles(GuiGraphics guiGraphics) {
        int size = 10;
        int padding = 4;

        int lx = leftGroup.x + leftGroup.width - size - padding;
        int ly = leftGroup.y + leftGroup.height - size - padding;
        guiGraphics.fill(lx, ly, lx + size, ly + size, 0xFFAAAAAA);

        int rx = rightGroup.x + rightGroup.width - size - padding;
        int ry = rightGroup.y + rightGroup.height - size - padding - 10; // raised a bit
        guiGraphics.fill(rx, ry, rx + size, ry + size, 0xFFAAAAAA);
    }

    private static float clamp01(float v) {
        if (v < 0.0F) return 0.0F;
        if (v > 1.0F) return 1.0F;
        return v;
    }

    private static class FloatSlider extends AbstractSliderButton {

        private final String label;
        private final DoubleSupplier getter;
        private final DoubleConsumer setter;

        public FloatSlider(int x, int y, int width, int height,
                           String label,
                           DoubleSupplier getter,
                           DoubleConsumer setter) {
            super(x, y, width, height, Component.empty(), getter.getAsDouble());
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(label + ": " + String.format("%.2f", this.value)));
        }

        @Override
        protected void applyValue() {
            setter.accept(this.value);
        }
    }

    private boolean insidePreview(double mouseX, double mouseY) {
        int cardTop = rightGroup.y + 12;
        int cardBottom = rightGroup.y + rightGroup.height - 28;
        int cardLeft = rightGroup.x;
        int cardRight = rightGroup.x + rightGroup.width;
        return mouseX >= cardLeft && mouseX <= cardRight && mouseY >= cardTop && mouseY <= cardBottom;
    }

}
