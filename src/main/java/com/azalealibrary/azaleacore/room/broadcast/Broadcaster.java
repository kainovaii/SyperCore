package com.azalealibrary.azaleacore.room.broadcast;

import com.azalealibrary.azaleacore.room.broadcast.message.Message;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Broadcaster {

    private final String prefix;
    private final World world;
    private final World lobby;

    public Broadcaster(String prefix, World world, World lobby) {
        this.prefix = prefix;
        this.world = world;
        this.lobby = lobby;
    }

    public void broadcast(Message message) {
        broadcast(message, Chanel.WORLD);
        broadcast(message, Chanel.LOBBY);
    }

    public void toWorld(Message message) {
        broadcast(message, Chanel.WORLD);
    }

    public void toLobby(Message message) {
        broadcast(message, Chanel.LOBBY);
    }

    public void toPlayer(Player player, Message message) {
//        if (world.getPlayers().contains(player) || lobby.getPlayers().contains(player)) {
            message.post(prefix, player); // TODO - review
//        }
    }

    public void broadcast(Message message, Chanel chanel) {
        switch (chanel) {
            case WORLD -> world.getPlayers().forEach(player -> message.post(prefix, player));
            case LOBBY -> lobby.getPlayers().forEach(player -> message.post(prefix, player));
        }
    }

    public enum Chanel {
        WORLD, LOBBY
    }
}