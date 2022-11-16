package com.azalealibrary.azaleacore.command;

import com.azalealibrary.azaleacore.api.AzaleaMinigameApi;
import com.azalealibrary.azaleacore.api.AzaleaRoomApi;
import com.azalealibrary.azaleacore.command.core.Arguments;
import com.azalealibrary.azaleacore.room.MinigameRoom;
import com.azalealibrary.azaleacore.room.broadcast.message.ChatMessage;
import com.azalealibrary.azaleacore.room.broadcast.message.Message;
import com.azalealibrary.azaleacore.util.FileUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;

import java.io.File;
import java.util.List;

@Commands(@Command(name = RoomCommand.NAME))
public class RoomCommand extends AzaleaCommand {

    protected static final String NAME = "!room";

    private static final String CREATE = "create";
    private static final String TERMINATE = "terminate";

    public RoomCommand(JavaPlugin plugin) {
        super(plugin, NAME);
        completeWhen(arguments -> arguments.size() == 1, (sender, arguments) -> List.of(CREATE, TERMINATE));
        completeWhen(arguments -> arguments.size() == 2 && arguments.get(0).equals(CREATE), (sender, arguments) -> AzaleaMinigameApi.getInstance().getMinigames().keySet().stream().toList());
        completeWhen(arguments -> arguments.size() == 2 && arguments.get(0).equals(TERMINATE), (sender, arguments) -> AzaleaRoomApi.getInstance().getRooms().stream().map(MinigameRoom::getName).toList());
        completeWhen(arguments -> arguments.size() == 3 && arguments.get(0).equals(CREATE), (sender, arguments) -> FileUtil.templates().stream().map(File::getName).toList());
        executeWhen(arguments -> arguments.get(0).equals(CREATE), this::create);
        executeWhen(arguments -> arguments.get(0).equals(TERMINATE), this::terminate);
    }

    private Message create(CommandSender sender, Arguments arguments) {
        AzaleaMinigameApi.MinigameProvider provider = arguments.parse(1, "", input -> AzaleaMinigameApi.getInstance().getMinigame(input));
        File template = arguments.parse(2, "", FileUtil::template);
        String name = arguments.missing(3);

        if (AzaleaRoomApi.getInstance().getRoom(name) != null) {
            return failure("Room '" + name + "' already exists.");
        }

        if (sender instanceof Player player) {
            AzaleaRoomApi.getInstance().createRoom(provider, name, player.getWorld(), template);

            return success("Room '" + name + "' created.");
        }
        return failure("Command issuer not a player.");
    }

    private Message terminate(CommandSender sender, Arguments arguments) {
        MinigameRoom room = arguments.parse(1, "Could not find room '%s'.", input -> AzaleaRoomApi.getInstance().getRoom(input));

        Message message = arguments.size() > 1
                ? new ChatMessage(String.join(" ", arguments.subList(1, arguments.size())))
                : new ChatMessage("Game ended by " + ChatColor.YELLOW + sender.getName() + ChatColor.RESET + ".");
        room.terminate(message);

        return success("Terminating room '" + room.getName() + "'.");
    }
}
