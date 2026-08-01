package com.hexandcube.signalloss.client;

import com.hexandcube.signalloss.commands.SignalLossCommands;
import com.hexandcube.signalloss.config.SignalLossConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalLossClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("signalloss");
    private static final Identifier HUD_LAYER = Identifier.fromNamespaceAndPath("signalloss", "toast_layer");

    public static volatile long lastPacketTime = System.nanoTime();
    private static boolean isSignalLost = false;
    private static long toastStartTime = 0;
    private static long lingerStartTime = 0;
    private static double displayedLagTime = 0;
    private static long joinTime = 0;
    private static float animationProgress = 0f;

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

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, HUD_LAYER, SignalLossClient::render);
    }

    private static void render(GuiGraphicsExtractor drawContext, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        if (!SignalLossConfig.INSTANCE.enabled) {
            if (isSignalLost || animationProgress > 0) {
                resetAll();
            }
            return;
        }

        if (client.isLocalServer() && !SignalLossConfig.INSTANCE.showInSingleplayer) {
            resetLogicState();
            return;
        }

        if (client.isPaused()) {
            lastPacketTime = System.nanoTime();
            resetLogicState();
            return;
        }

        long currentNanoTime = System.nanoTime();

        float deltaSeconds = tickCounter.getGameTimeDeltaTicks() / 20.0f;

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
        animationProgress = Mth.clamp(animationProgress, 0f, 1f);

        if (animationProgress > 0) {
            double displayTime = isOverThreshold ? lagSeconds : displayedLagTime;
            String timeString = String.format("%.1f", displayTime);
            Component text = Component.literal(String.format(SignalLossConfig.INSTANCE.toastMessage + " (%ss)", timeString));
            renderToast(drawContext, client.font, client.getWindow().getGuiScaledWidth(), text, animationProgress);
        }
    }

    private static void resetAll() {
        lastPacketTime = System.nanoTime();
        joinTime = System.nanoTime();
        resetLogicState();
        animationProgress = 0f;
    }

    private static void resetLogicState() {
        isSignalLost = false;
        toastStartTime = 0;
        lingerStartTime = 0;
        displayedLagTime = 0;
    }

    private static void renderToast(GuiGraphicsExtractor context, Font textRenderer, int screenWidth, Component text, float progress) {
        float easedProgress = 1 - (1 - progress) * (1 - progress);

        int textWidth = textRenderer.width(text);
        int textHeight = textRenderer.lineHeight;
        int padding = 6;
        int totalHeight = textHeight + (padding * 2);

        SignalLossConfig.ToastPosition pos = SignalLossConfig.INSTANCE.toastPosition;

        int x = switch (pos) {
            case LEFT -> 10;
            case CENTER -> (screenWidth - textWidth) / 2;
            case RIGHT -> screenWidth - textWidth - 10;
        };

        int hiddenY = -totalHeight - 5;
        int visibleY = 10;
        int y = (int) Mth.lerpInt(easedProgress, hiddenY, visibleY);

        if (SignalLossConfig.INSTANCE.drawBackground) {
            context.fill(x - padding, y - padding, x + textWidth + padding, y + textHeight + padding, SignalLossConfig.INSTANCE.backgroundColor);
        }

        context.text(textRenderer, text, x, y, SignalLossConfig.INSTANCE.textColor, true);
    }
}