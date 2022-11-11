package com.azalealibrary.azaleacore.command;

import com.azalealibrary.azaleacore.AzaleaApi;
import com.azalealibrary.azaleacore.broadcast.message.ChatMessage;
import com.azalealibrary.azaleacore.broadcast.message.Message;
import com.azalealibrary.azaleacore.minigame.MinigameRoom;
import com.azalealibrary.azaleacore.util.FileUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Optional;

@Commands(@Command(name = RoomCommand.NAME))
public class RoomCommand extends AzaleaCommand {

    protected static final String NAME = AzaleaCommand.COMMAND_PREFIX + "room";

    private static final String CREATE = "CREATE";
    private static final String TERMINATE = "TERMINATE";

    public RoomCommand(JavaPlugin plugin) {
        super(plugin, NAME);
    }

    @Override
    protected Message execute(@Nonnull CommandSender sender, List<String> params) {
        String actionInput = params.get(0);

        switch (actionInput) {
            case CREATE -> {
                String minigameInput = params.get(1);
                AzaleaApi.MinigameProvider<?> provider = AzaleaApi.getInstance().getMinigame(minigameInput);
                if (provider == null) {
                    return notFound("minigame", minigameInput);
                }

                String templateInput = params.get(2);
                Optional<String> template = FileUtil.templates().stream().map(File::getName)
                        .filter(n -> n.equals(templateInput))
                        .findFirst();
                if (template.isEmpty()) {
                    return notFound("template", templateInput);
                }

                String nameInput = params.size() > 3 ? params.get(3) : null;
                if (nameInput == null) {
                    return invalid("name", ChatColor.ITALIC + "<empty>");
                } else if (AzaleaApi.getInstance().getRoom(nameInput) != null) {
                    return failure("Room '" + nameInput + "' already exists.");
                }

                AzaleaApi.getInstance().createRoom(provider, nameInput, ((Player) sender).getWorld(), template.get());
                return success("Room '" + nameInput + "' created for minigame '" + minigameInput + "'.");
            }
            case TERMINATE -> {
                String roomInput = params.get(1);
                MinigameRoom room = AzaleaApi.getInstance().getRoom(roomInput);
                if (room == null) {
                    return notFound("room", roomInput);
                }

                room.terminate(params.size() > 2 ? new ChatMessage(String.join(" ", params.subList(3, params.size()))) : null);
                return success("Terminating room '" + roomInput + "'.");
            }
        }
        return invalid("action", actionInput);
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, List<String> params) {
        if (params.size() == 1) {
            return List.of(CREATE, TERMINATE);
        } else {
            String action = params.get(0);

            if (params.size() == 2) {
                if (action.equals(CREATE)) {
                    return AzaleaApi.getInstance().getMinigames().keySet().stream().toList();
                } else if (action.equals(TERMINATE)) {
                    return AzaleaApi.getInstance().getRooms().stream().map(MinigameRoom::getName).toList();
                }
            } else if (params.size() == 3 && action.equals(CREATE)) {
                return FileUtil.templates().stream().map(File::getName).toList();
            }
        }
        return List.of();
    }
}
