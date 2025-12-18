package com.hexandcube.signalloss.commands;

import com.hexandcube.signalloss.config.SignalLossConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SignalLossCommands {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("signalloss");

        root.then(ClientCommandManager.literal("reload")
                .executes(context -> {
                    SignalLossConfig.load();
                    context.getSource().sendFeedback(Text.translatable("signalloss.command.reload").formatted(Formatting.GREEN));
                    return 1;
                })
        );

        LiteralArgumentBuilder<FabricClientCommandSource> config = ClientCommandManager.literal("config");

        config.then(ClientCommandManager.literal("reset")
                .executes(context -> {
                    SignalLossConfig.INSTANCE.reset();
                    SignalLossConfig.save();
                    context.getSource().sendFeedback(Text.translatable("signalloss.command.reset").formatted(Formatting.GREEN));
                    return 1;
                })
        );

        config.then(ClientCommandManager.literal("enabled")
                .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean val = BoolArgumentType.getBool(context, "value");
                            SignalLossConfig.INSTANCE.enabled = val;
                            SignalLossConfig.save();
                            context.getSource().sendFeedback(Text.translatable("signalloss.command.set.enabled", val).formatted(Formatting.YELLOW));
                            return 1;
                        })));

        config.then(ClientCommandManager.literal("timeoutThreshold")
                .then(ClientCommandManager.argument("milliseconds", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int val = IntegerArgumentType.getInteger(context, "milliseconds");
                            SignalLossConfig.INSTANCE.timeoutThreshold = val;
                            SignalLossConfig.save();
                            context.getSource().sendFeedback(Text.translatable("signalloss.command.set.timeout", val).formatted(Formatting.YELLOW));
                            return 1;
                        })));

        config.then(ClientCommandManager.literal("minWarningTime")
                .then(ClientCommandManager.argument("milliseconds", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int val = IntegerArgumentType.getInteger(context, "milliseconds");
                            SignalLossConfig.INSTANCE.minWarningTime = val;
                            SignalLossConfig.save();
                            context.getSource().sendFeedback(Text.translatable("signalloss.command.set.min_warning", val).formatted(Formatting.YELLOW));
                            return 1;
                        })));

        config.then(ClientCommandManager.literal("lingerTime")
                .then(ClientCommandManager.argument("milliseconds", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int val = IntegerArgumentType.getInteger(context, "milliseconds");
                            SignalLossConfig.INSTANCE.lingerTime = val;
                            SignalLossConfig.save();
                            context.getSource().sendFeedback(Text.translatable("signalloss.command.set.linger", val).formatted(Formatting.YELLOW));
                            return 1;
                        })));

        config.then(ClientCommandManager.literal("drawBackground")
                .then(ClientCommandManager.argument("visible", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean val = BoolArgumentType.getBool(context, "visible");
                            SignalLossConfig.INSTANCE.drawBackground = val;
                            SignalLossConfig.save();
                            context.getSource().sendFeedback(Text.translatable("signalloss.command.set.background", val).formatted(Formatting.YELLOW));
                            return 1;
                        })));

        config.then(ClientCommandManager.literal("textColor")
                .then(ClientCommandManager.argument("hex", StringArgumentType.word())
                        .executes(context -> {
                            String hex = StringArgumentType.getString(context, "hex");
                            try {
                                int color = parseColor(hex);
                                SignalLossConfig.INSTANCE.textColor = color;
                                SignalLossConfig.save();
                                context.getSource().sendFeedback(Text.translatable("signalloss.command.set.textcolor", String.format("#%08X", color)).formatted(Formatting.YELLOW));
                                return 1;
                            } catch (NumberFormatException e) {
                                context.getSource().sendError(Text.translatable("signalloss.command.error.color"));
                                return 0;
                            }
                        })));

        config.then(ClientCommandManager.literal("backgroundColor")
                .then(ClientCommandManager.argument("hex", StringArgumentType.word())
                        .executes(context -> {
                            String hex = StringArgumentType.getString(context, "hex");
                            try {
                                int color = parseColor(hex);
                                SignalLossConfig.INSTANCE.backgroundColor = color;
                                SignalLossConfig.save();
                                context.getSource().sendFeedback(Text.translatable("signalloss.command.set.bgcolor", String.format("#%08X", color)).formatted(Formatting.YELLOW));
                                return 1;
                            } catch (NumberFormatException e) {
                                context.getSource().sendError(Text.translatable("signalloss.command.error.color"));
                                return 0;
                            }
                        })));

        config.then(ClientCommandManager.literal("showInSingleplayer")
                .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean val = BoolArgumentType.getBool(context, "enabled");
                            SignalLossConfig.INSTANCE.showInSingleplayer = val;
                            SignalLossConfig.save();
                            context.getSource().sendFeedback(Text.translatable("signalloss.command.set.singleplayer", val).formatted(Formatting.YELLOW));
                            return 1;
                        })));

        config.then(ClientCommandManager.literal("position")
                .then(ClientCommandManager.argument("pos", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"LEFT", "CENTER", "RIGHT"}, builder))
                        .executes(context -> {
                            String input = StringArgumentType.getString(context, "pos").toUpperCase();
                            try {
                                SignalLossConfig.ToastPosition newPos = SignalLossConfig.ToastPosition.valueOf(input);
                                SignalLossConfig.INSTANCE.toastPosition = newPos;
                                SignalLossConfig.save();
                                context.getSource().sendFeedback(Text.translatable("signalloss.command.set.position", newPos.name()).formatted(Formatting.YELLOW));
                                return 1;
                            } catch (IllegalArgumentException e) {
                                context.getSource().sendError(Text.translatable("signalloss.command.error.position"));
                                return 0;
                            }
                        })));

        root.then(config);
        dispatcher.register(root);
    }

    private static int parseColor(String input) throws NumberFormatException {
        if (input.startsWith("#")) input = input.substring(1);
        else if (input.startsWith("0x")) input = input.substring(2);

        if (input.length() != 6 && input.length() != 8) {
            throw new NumberFormatException("Invalid hex length");
        }

        long colorVal = Long.parseLong(input, 16);

        if (input.length() == 6) {
            colorVal |= 0xFF000000L;
        }

        return (int) colorVal;
    }
}