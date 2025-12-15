package com.hexandcube.signalloss.client;

import com.hexandcube.signalloss.commands.SignalLossCommands;
import com.hexandcube.signalloss.config.SignalLossConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalLossClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("signalloss");

    public static volatile long lastPacketTime = System.nanoTime();

    private static boolean isSignalLost = false;
    private static long toastStartTime = 0;
    private static long lingerStartTime = 0;
    private static double displayedLagTime = 0;
    private static long joinTime = 0;

    private static float animationProgress = 0f;
    private static long lastRenderTime = System.nanoTime();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing SignalLoss...");

        SignalLossConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            SignalLossCommands.register(dispatcher);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resetAll();
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;

            if (!SignalLossConfig.INSTANCE.enabled) {
                if (isSignalLost || animationProgress > 0) {
                    resetAll();
                }
                return;
            }

            if (client.isInSingleplayer() && !SignalLossConfig.INSTANCE.showInSingleplayer) {
                resetLogicState();
                return;
            }

            if (client.isPaused()) {
                lastPacketTime = System.nanoTime();
                resetLogicState();
                return;
            }

            long currentNanoTime = System.nanoTime();

            float deltaSeconds = (currentNanoTime - lastRenderTime) / 1_000_000_000f;
            lastRenderTime = currentNanoTime;

            long nanoDiff = currentNanoTime - lastPacketTime;
            long msSinceLastPacket = nanoDiff / 1_000_000;
            double lagSeconds = nanoDiff / 1_000_000_000.0;

            boolean inGracePeriod = (currentNanoTime - joinTime) / 1_000_000 < 5000;

            int thresholdMs = SignalLossConfig.INSTANCE.timeoutThreshold;
            int minWarningMs = SignalLossConfig.INSTANCE.minWarningTime;
            int lingerMs = SignalLossConfig.INSTANCE.lingerTime;

            boolean isOverThreshold = msSinceLastPacket > thresholdMs;
            boolean shouldShowToast = false;

            if (isOverThreshold) {
                if (!inGracePeriod) {
                    if (toastStartTime == 0) toastStartTime = currentNanoTime;
                    isSignalLost = true;
                    lingerStartTime = 0;
                    displayedLagTime = lagSeconds;
                    shouldShowToast = true;
                }
            } else if (isSignalLost) {
                if (lingerStartTime == 0) lingerStartTime = currentNanoTime;

                long msShownTotal = (currentNanoTime - toastStartTime) / 1_000_000;
                long msLingered = (currentNanoTime - lingerStartTime) / 1_000_000;

                if (msShownTotal < minWarningMs || msLingered < lingerMs) {
                    shouldShowToast = true;
                } else {
                    resetLogicState();
                    shouldShowToast = false;
                }
            }

            float animationSpeed = 4.0f;
            if (shouldShowToast) {
                animationProgress += deltaSeconds * animationSpeed;
            } else {
                animationProgress -= deltaSeconds * animationSpeed;
            }
            animationProgress = MathHelper.clamp(animationProgress, 0f, 1f);

            if (animationProgress > 0) {
                double displayTime = isOverThreshold ? lagSeconds : displayedLagTime;

                String timeString = String.format("%.1f", displayTime);
                Text text = Text.translatable("signalloss.toast.lost", timeString);
                renderToast(drawContext, client.textRenderer, client.getWindow().getScaledWidth(), text, animationProgress);
            }
        });
    }

    private static void resetAll() {
        lastPacketTime = System.nanoTime();
        joinTime = System.nanoTime();
        lastRenderTime = System.nanoTime();
        resetLogicState();
        animationProgress = 0f;
    }

    private static void resetLogicState() {
        isSignalLost = false;
        toastStartTime = 0;
        lingerStartTime = 0;
        displayedLagTime = 0;
    }

    private static void renderToast(DrawContext context, TextRenderer textRenderer, int screenWidth, Text text, float progress) {
        float easedProgress = 1 - (1 - progress) * (1 - progress);

        int textWidth = textRenderer.getWidth(text);
        int textHeight = textRenderer.fontHeight;
        int padding = 6;
        int totalHeight = textHeight + (padding * 2);

        SignalLossConfig.ToastPosition pos = SignalLossConfig.INSTANCE.toastPosition;

        int x = 0;
        int offset = 10;

        x = switch (pos) {
            case LEFT -> offset;
            case CENTER -> (screenWidth - textWidth) / 2;
            case RIGHT -> screenWidth - textWidth - offset;
        };

        int hiddenY = -totalHeight - 5;
        int visibleY = offset;
        int y = (int) MathHelper.lerp(easedProgress, hiddenY, visibleY);

        int backgroundColor = SignalLossConfig.INSTANCE.backgroundColor;
        int textColor = SignalLossConfig.INSTANCE.textColor;

        if (SignalLossConfig.INSTANCE.drawBackground) {
            context.fill(x - padding, y - padding, x + textWidth + padding, y + textHeight + padding, backgroundColor);
        }

        context.drawTextWithShadow(textRenderer, text, x, y, textColor);
    }
}