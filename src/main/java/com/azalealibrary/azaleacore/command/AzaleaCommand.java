package com.azalealibrary.azaleacore.command;

import com.azalealibrary.azaleacore.api.broadcast.message.Message;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// TODO - currently composition-based, what about inheritance?
public abstract class AzaleaCommand implements CommandExecutor, TabCompleter {

    public static final String COMMAND_PREFIX = "!";
    private static final String COMMAND_TEXT_PREFIX = "AZA";

    protected AzaleaCommand(JavaPlugin plugin, String name) {
        PluginCommand command = plugin.getCommand(name);

        if (command == null) {
            throw new IllegalArgumentException("Azalea command with name '" + name + "' does not exist.");
        }

        command.setExecutor(this);
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String ignored, @Nonnull String[] args) {
        execute(sender, Arrays.asList(args)).post(COMMAND_TEXT_PREFIX, sender);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        List<String> params = Arrays.asList(args);
        List<String> output = new ArrayList<>(onTabComplete(sender, params));
        String last = params.size() > 0 ? params.get(params.size() - 1) : "";
        output.sort(Comparator.<String, Boolean>comparing(s -> s.contains(last)).reversed());
        return output;
    }

    protected abstract Message execute(@Nonnull CommandSender sender, List<String> params);

    protected abstract List<String> onTabComplete(CommandSender sender, List<String> params);
}