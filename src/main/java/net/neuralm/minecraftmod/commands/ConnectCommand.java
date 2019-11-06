package net.neuralm.minecraftmod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.neuralm.client.NeuralmClient;
import net.neuralm.client.messages.serializer.JsonSerializer;
import net.neuralm.minecraftmod.Neuralm;
import net.neuralm.minecraftmod.SingleUseListener;
import net.neuralm.minecraftmod.commands.LoginCommand.StringArgument;

import java.io.IOException;

public class ConnectCommand {

    /**
     * Register the connect command
     *
     * @param commandDispatcher The command dispatcher to register to
     */
    public static void register(CommandDispatcher<CommandSource> commandDispatcher) {

        //Register a command that starts with /connect and needs an ip (String) and port (int) argument
        //Only operators can use it

        commandDispatcher.register(
                Commands.literal("connect").requires((source) -> source.hasPermissionLevel(2))
                        .then(
                                Commands.argument("ip", new StringArgument()).then(
                                        Commands.argument("port", IntegerArgumentType.integer()).then(
                                                Commands.argument("force", BoolArgumentType.bool()).executes(ConnectCommand::connectForce)
                                        ).executes(ConnectCommand::connect)
                                )
                        )
        );
    }

    private static int connectForce(CommandContext<CommandSource> context) {
        if (context.getArgument("force", boolean.class)) {
//TODO            Neuralm.instance.client.disconnect(); No disconnect method yet
            Neuralm.instance.client = null;
        }

        return connect(context);
    }

    /**
     * Try and connect to the server using the passed in ip and port
     */
    private static int connect(CommandContext<CommandSource> context) {
        String ip = context.getArgument("ip", String.class);
        int port = context.getArgument("port", int.class);

        if (Neuralm.instance.client != null) {
            context.getSource().sendFeedback(new TranslationTextComponent("neuralm.already_connected", ip, port).setStyle(new Style().setColor(TextFormatting.YELLOW)), true);
            return -1;
        }

        Neuralm.instance.client = new NeuralmClient(ip, port, new JsonSerializer(), false, -1);

        new SingleUseListener(evt -> {
            context.getSource().getServer().runAsync(() -> {
                context.getSource().sendFeedback(new TranslationTextComponent("neuralm.connected", ip, port).setStyle(new Style().setColor(TextFormatting.GREEN)), true);
            });
        }, "Connected");

        try {
            Neuralm.instance.client.start();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return Command.SINGLE_SUCCESS;
    }

}
