package net.neuralm.minecraftmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.neuralm.client.messages.requests.RegisterRequest;
import net.neuralm.client.messages.responses.Response;
import net.neuralm.minecraftmod.Neuralm;
import net.neuralm.minecraftmod.SingleUseListener;

public class RegisterCommand {

    public static void register(CommandDispatcher<CommandSource> commandDispatcher) {

        //Register a command that starts with /register and needs an username (String) and password (String) argument
        //Only operators can use it

        commandDispatcher.register(
            Commands.literal("register").requires((source) -> source.hasPermissionLevel(2))
                .then(
                    Commands.argument("username", new LoginCommand.StringArgument()).then(
                        Commands.argument("password", new LoginCommand.StringArgument()).executes(RegisterCommand::register)
                    )
                )
        );
    }

    private static int register(CommandContext<CommandSource> context) {

        if (Neuralm.instance.client == null) {
            context.getSource().sendFeedback(new TranslationTextComponent("neuralm.not_connected"), true);
            return -1;
        }

        new SingleUseListener((evt) -> {
            Response response = (Response) evt.getNewValue();
            context.getSource().getServer().runAsync(() -> {
                if (response.isSuccess()) {
                    context.getSource().sendFeedback(new TranslationTextComponent("neuralm.register.success"), true);
                } else {
                    context.getSource().sendFeedback(new TranslationTextComponent("neuralm.register.failed", response.getMessage()), true);
                }
            });

        }, "RegisterResponse");

        Neuralm.instance.client.send(new RegisterRequest(context.getArgument("username", String.class), context.getArgument("password", String.class), "Name"));

        return 1;
    }

}
