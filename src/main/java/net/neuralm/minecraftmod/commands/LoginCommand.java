package net.neuralm.minecraftmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.neuralm.client.messages.requests.AuthenticateRequest;
import net.neuralm.client.messages.responses.AuthenticateResponse;
import net.neuralm.minecraftmod.Neuralm;
import net.neuralm.minecraftmod.SingleUseListener;

public class LoginCommand {

    /**
     * Register the login command
     *
     * @param commandDispatcher The command dispatcher to register to
     */
    public static void register(CommandDispatcher<CommandSource> commandDispatcher) {

        //Register a command that starts with /login and needs an username (String) and password (String) argument
        //Only operators can use it

        commandDispatcher.register(
            Commands.literal("login").requires((source) -> source.hasPermissionLevel(2))
                .then(
                    Commands.argument("username", new StringArgument()).then(
                        Commands.argument("password", new StringArgument()).executes(LoginCommand::login)
                    )
                )
        );
    }

    private static int login(CommandContext<CommandSource> context) {

        if (Neuralm.instance.client == null) {
            context.getSource().sendFeedback(new TranslationTextComponent("neuralm.not_connected").setStyle(new Style().setColor(TextFormatting.RED)), true);
            return -1;
        }

        new SingleUseListener((evt) -> {
            AuthenticateResponse response = (AuthenticateResponse) evt.getNewValue();
            context.getSource().getServer().runAsync(() -> {
                if (response.isSuccess()) {
                    context.getSource().sendFeedback(new TranslationTextComponent("neuralm.login.success").setStyle(new Style().setColor(TextFormatting.GREEN)), true);
                } else {
                    context.getSource().sendFeedback(new TranslationTextComponent("neuralm.login.failed", response.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)), true);
                }
            });

        }, "AuthenticateResponse");

        Neuralm.instance.client.send(new AuthenticateRequest(context.getArgument("username", String.class), context.getArgument("password", String.class), "Name"));

        return 1;
    }

    static class StringArgument implements ArgumentType<String> {

        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            return reader.readString();
        }
    }
}
