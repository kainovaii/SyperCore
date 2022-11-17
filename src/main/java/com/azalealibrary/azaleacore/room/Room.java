package com.azalealibrary.azaleacore.room;

import com.azalealibrary.azaleacore.AzaleaCore;
import com.azalealibrary.azaleacore.api.AzaleaMinigameApi;
import com.azalealibrary.azaleacore.api.AzaleaRoomApi;
import com.azalealibrary.azaleacore.api.core.Minigame;
import com.azalealibrary.azaleacore.foundation.AzaleaException;
import com.azalealibrary.azaleacore.room.broadcast.Broadcaster;
import com.azalealibrary.azaleacore.room.broadcast.message.ChatMessage;
import com.azalealibrary.azaleacore.room.broadcast.message.Message;
import com.azalealibrary.azaleacore.round.RoundConfiguration;
import com.azalealibrary.azaleacore.round.RoundTicker;
import com.azalealibrary.azaleacore.util.FileUtil;
import com.azalealibrary.azaleacore.util.ScheduleUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Room {

    private final String name;
    private final Playground playground;
    private final World lobby;
    private final World world;
    private final Minigame minigame;
    private final RoundTicker roundTicker;
    private final SignTicker signTicker;
    private final Broadcaster broadcaster;
    private final RoundConfiguration configuration;

    private boolean hasIssuedTask = false;

    public Room(String name, Playground playground, World lobby, World world, Minigame minigame) {
        this.name = name;
        this.playground = playground;
        this.lobby = lobby;
        this.world = world;
        this.minigame = minigame;
        this.configuration = RoundConfiguration.create(AzaleaCore.INSTANCE) // TODO - review
                .graceDuration(3)
                .roundDuration(30)
                .tickRate(1)
                .build();
        this.roundTicker = new RoundTicker(this, this.configuration);
        this.signTicker = new SignTicker(this);
        this.broadcaster = new Broadcaster(name, world, lobby);
    }

    public String getName() {
        return name;
    }

    public Playground getPlayground() {
        return playground;
    }

    public World getLobby() {
        return lobby;
    }

    public World getWorld() {
        return world;
    }

    public <M extends Minigame> M getMinigame() {
        return (M) minigame;
    }

    public RoundTicker getRoundTicker() {
        return roundTicker;
    }

    public SignTicker getSignTicker() {
        return signTicker;
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }

    public void start(@Nullable Message message) {
        if (roundTicker.isRunning()) {
            throw new AzaleaException("Cannot begin round while round is already running.");
        }

        delay("Minigame starting in %s...", () -> start(world.getPlayers(), message));
    }

    private void start(List<Player> players, @Nullable Message message) {
        roundTicker.begin(minigame.newRound(configuration, players));

        if (message != null) {
            broadcaster.broadcast(message);
        }
    }

    public void stop(@Nullable Message message) {
        if (!roundTicker.isRunning()) {
            throw new AzaleaException("Cannot end round while round is not running.");
        }

        roundTicker.cancel();

        if (message != null) {
            broadcaster.broadcast(message);
        }
    }

    public void restart(@Nullable Message message) {
        stop(null);
        start(world.getPlayers(), message);
    }

    public void teleportAllToLobby() {
        world.getPlayers().forEach(p -> p.teleport(lobby.getSpawnLocation()));
    }

    public void teleportAllToWorld() {
        world.getPlayers().forEach(p -> p.teleport(playground.getSpawn()));
    }

    public void terminate(@Nullable Message message) {
        if (roundTicker.isRunning()) {
            stop(message);
        }

        delay("Terminating room in %s...", () -> {
            teleportAllToLobby();
            signTicker.discardAll();
            AzaleaRoomApi.getInstance().remove(this);
            Bukkit.unloadWorld(world, false);
            FileUtil.delete(FileUtil.room(name));
        });
    }

    private void delay(String message, Runnable done) {
        if (hasIssuedTask) {
            throw new AzaleaException("Command already under way.");
        }

        hasIssuedTask = true;
        AtomicInteger countdown = new AtomicInteger(3);
        ScheduleUtil.doWhile(countdown.get() * 20, 20, () -> {
            String info = String.format(message, countdown.decrementAndGet() + 1);
            broadcaster.toPlayground(new ChatMessage(ChatColor.YELLOW + info));
        }, () -> {
            done.run();
            hasIssuedTask = false;
        });
    }

    public static Room create(String name, Playground playground, World lobby, AzaleaMinigameApi.MinigameProvider provider) {
        // TODO - separate thread - https://pastebin.com/K9CuVMS5
        FileUtil.copyDirectory(playground.getTemplate(), new File(FileUtil.ROOMS, name));
        World world = Bukkit.createWorld(new WorldCreator("rooms/" + name));

        Minigame minigame = provider.create(world);
        return new Room(name, playground, lobby, world, minigame);
    }
}
