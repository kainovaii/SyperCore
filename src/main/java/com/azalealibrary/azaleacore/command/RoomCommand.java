package com.azalealibrary.azaleacore.command;

import com.azalealibrary.azaleacore.api.AzaleaPlaygroundApi;
import com.azalealibrary.azaleacore.api.AzaleaRoomApi;
import com.azalealibrary.azaleacore.command.core.*;
import com.azalealibrary.azaleacore.foundation.AzaleaException;
import com.azalealibrary.azaleacore.foundation.broadcast.AzaleaBroadcaster;
import com.azalealibrary.azaleacore.foundation.broadcast.message.ChatMessage;
import com.azalealibrary.azaleacore.foundation.broadcast.message.Message;
import com.azalealibrary.azaleacore.foundation.registry.AzaleaRegistry;
import com.azalealibrary.azaleacore.foundation.registry.MinigameIdentifier;
import com.azalealibrary.azaleacore.minigame.Minigame;
import com.azalealibrary.azaleacore.room.Playground;
import com.azalealibrary.azaleacore.room.Room;
import com.azalealibrary.azaleacore.util.FileUtil;
import com.azalealibrary.azaleacore.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Objects;

@AzaCommand(name = "!room")
public class RoomCommand extends AzaleaCommand {

    private static final String CREATE = "create";
    private static final String TERMINATE = "terminate";

    private static final String NEW = "new";
    private static final String COPY = "copy";

    public RoomCommand(CommandDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    protected void configure(CommandConfigurator configurator) {
        configurator.completeWhen((sender, arguments) -> arguments.size() == 1, (sender, arguments) -> List.of(CREATE, TERMINATE));
        configurator.completeWhen((sender, arguments) -> arguments.size() == 2 && arguments.is(0, CREATE), (sender, arguments) -> List.of(NEW, COPY));
        configurator.completeWhen((sender, arguments) -> arguments.size() == 2 && arguments.is(0, TERMINATE), (sender, arguments) -> AzaleaRoomApi.getInstance().getKeys());
        configurator.completeWhen((sender, arguments) -> arguments.size() == 3 && arguments.is(1, NEW), (sender, arguments) -> AzaleaRegistry.MINIGAME.getObjects().stream().map(MinigameIdentifier::getNamespace).toList());
        configurator.completeWhen((sender, arguments) -> arguments.size() == 4 && arguments.is(1, NEW), (sender, arguments) -> FileUtil.maps().stream().map(File::getName).toList());
        configurator.completeWhen((sender, arguments) -> arguments.size() == 3 && arguments.is(1, COPY), (sender, arguments) -> AzaleaPlaygroundApi.getInstance().getKeys());
        configurator.executeWhen((sender, arguments) -> arguments.is(0, TERMINATE), this::terminate);
        configurator.executeWhen((sender, arguments) -> arguments.is(1, NEW), this::createNew);
        configurator.executeWhen((sender, arguments) -> arguments.is(1, COPY), this::createCopy);
    }

    private Message createNew(CommandSender sender, Arguments arguments) {
        MinigameIdentifier identifier = arguments.find(2, "minigame", input -> AzaleaRegistry.MINIGAME.getObjects().stream().filter(key -> Objects.equals(key.getNamespace(), input)).findFirst().orElse(null));
        File map = arguments.find(3, "map", FileUtil::map);
        String name = arguments.notMissing(4, "name");

        return createRoom(sender, name, Minigame.create(identifier), map);
    }

    private Message createCopy(CommandSender sender, Arguments arguments) {
        Playground playground = arguments.find(2, "playground", AzaleaPlaygroundApi.getInstance()::get);
        String name = arguments.notMissing(3, "name");

        return createRoom(sender, name, playground.getMinigame(), playground.getMap());
    }

    private static Message createRoom(CommandSender sender, String name, Minigame minigame, File map) {
        if (AzaleaRoomApi.getInstance().hasKey(name)) {
            throw new AzaleaException("Room '" + name + "' already exists.");
        }

        AzaleaBroadcaster.getInstance().send(sender, ChatMessage.info("Creating room '" + name + "'..."));
        AzaleaRoomApi.getInstance().createRoom((Player) sender, name, map, minigame);

        return ChatMessage.info("Room '" + name + "' created.");
    }

    private Message terminate(CommandSender sender, Arguments arguments) {
        Room room = arguments.find(1, "room", AzaleaRoomApi.getInstance()::get);

        Message message = arguments.size() > 1
                ? ChatMessage.info(String.join(" ", arguments.subList(1, arguments.size())))
                : ChatMessage.info("Game ended by " + TextUtil.getName((Player) sender) + ".");
        AzaleaRoomApi.getInstance().terminateRoom(room, message);

        return ChatMessage.info("Terminating room " + TextUtil.getName(room) + ".");
    }
}
