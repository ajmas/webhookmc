package org.terraazure.webhookmc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;

import net.kyori.adventure.text.Component;

public class WebhookCommand implements Command.Raw {
    private final Component usage = Component.text("<message>");
    private final Component description = Component.text("Display a message to all players");

    @Override
    public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
        String message = arguments.remaining();
        Sponge.server().broadcastAudience().sendMessage(Component.text(message));
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean canExecute(CommandCause cause) {
        return cause.hasPermission("myplugin.broadcast");
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.of(this.description);
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public Component usage(CommandCause cause) {
        return this.usage;
    }
}
