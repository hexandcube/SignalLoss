package com.hexandcube.signalloss.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hexandcube.signalloss.client.SignalLossClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SignalLossConfig {

    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("signalloss.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static SignalLossConfig INSTANCE;

    public boolean enabled = true;
    public int timeoutThreshold = 2000;
    public int minWarningTime = 2000;
    public int lingerTime = 1000;
    public boolean drawBackground = true;
    public boolean showInSingleplayer = false;
    public ToastPosition toastPosition = ToastPosition.CENTER;
    public int textColor = 0xFFFF5555;
    public int backgroundColor = 0xA0000000;

    public enum ToastPosition {
        LEFT, CENTER, RIGHT
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, SignalLossConfig.class);
            } catch (IOException e) {
                SignalLossClient.LOGGER.error("Failed to load SignalLoss config! Defaulting to standard settings.", e);
                INSTANCE = new SignalLossConfig();
            }
        } else {
            INSTANCE = new SignalLossConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            SignalLossClient.LOGGER.error("Failed to save SignalLoss config!", e);
        }
    }

    public void reset() {
        this.enabled = true;
        this.timeoutThreshold = 2000;
        this.minWarningTime = 2000;
        this.lingerTime = 1000;
        this.drawBackground = true;
        this.showInSingleplayer = false;
        this.toastPosition = ToastPosition.CENTER;
        this.textColor = 0xFFFF5555;
        this.backgroundColor = 0xA0000000;
    }
}