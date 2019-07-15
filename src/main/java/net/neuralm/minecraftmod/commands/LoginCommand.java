package net.neuralm.minecraftmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.neuralm.client.messages.requests.AuthenticateRequest;
import net.neuralm.client.messages.responses.AuthenticateResponse;
import net.neuralm.minecraftmod.Neuralm;

public class LoginCommand {

    public static void register(CommandDispatcher<CommandSource> commandDispatcher) {
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

        Neuralm.instance.client.addListener("AuthenticateResponse", evt -> {
            AuthenticateResponse response = (AuthenticateResponse) evt.getNewValue();

            if(response.isSuccess()) {
                context.getSource().sendFeedback(new StringTextComponent("Login successful!"), true);
            } else {
                context.getSource().sendFeedback(new StringTextComponent("Login failed!"), true);
            }
        });

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
