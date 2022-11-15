package com.azalealibrary.azaleacore.room;

import com.azalealibrary.azaleacore.AzaleaApi;
import com.azalealibrary.azaleacore.Main;
import com.azalealibrary.azaleacore.api.Minigame;
import com.azalealibrary.azaleacore.broadcast.Broadcaster;
import com.azalealibrary.azaleacore.broadcast.message.ChatMessage;
import com.azalealibrary.azaleacore.broadcast.message.Message;
import com.azalealibrary.azaleacore.minigame.round.RoundConfiguration;
import com.azalealibrary.azaleacore.minigame.round.RoundTicker;
import com.azalealibrary.azaleacore.util.FileUtil;
import com.azalealibrary.azaleacore.util.ScheduleUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MinigameRoom {

    private final String name;
    private final World world;
    private final World lobby;
    private final Minigame minigame;
    private final RoundTicker ticker;
    private final Broadcaster broadcaster;
    private final RoundConfiguration configuration;

    public MinigameRoom(String name, World world, World lobby, Minigame minigame) {
        this.name = name;
        this.world = world;
        this.lobby = lobby;
        this.minigame = minigame;
        this.configuration = RoundConfiguration.create(Main.INSTANCE) // TODO - review
                .graceDuration(3)
                .roundDuration(30)
                .tickRate(1)
                .build();
        this.ticker = new RoundTicker(this, this.configuration);
        this.broadcaster = new Broadcaster(name, world, lobby);
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return world;
    }

    public World getLobby() {
        return lobby;
    }

    public <M extends Minigame> M getMinigame() {
        return (M) minigame;
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }

    public void start(@Nullable Message message) {
        delay("Minigame starting in %s...", () -> start(world.getPlayers(), message));
    }

    private void start(List<Player> players, @Nullable Message message) {
        if (ticker.isRunning()) {
            throw new RuntimeException("Attempting to begin round while round is already running.");
        }

        ticker.begin(minigame.newRound(configuration, players));

        if (message != null) {
            broadcaster.broadcast(message);
        }
    }

    public void stop(@Nullable Message message) {
        if (!ticker.isRunning()) {
            throw new RuntimeException("Attempting to end round while round is not running.");
        }

        ticker.cancel();

        if (message != null) {
            broadcaster.broadcast(message);
        }
    }

    public void restart(@Nullable Message message) {
        stop(null);
        start(world.getPlayers(), message);
    }

    public void teleportToLobby() {
        Location location = lobby.getSpawnLocation().clone().add(0.5, 0, 0.5);
        lobby.getPlayers().forEach(p -> p.teleport(location));
        world.getPlayers().forEach(p -> p.teleport(location));
    }

    public void teleportToWorld() {
        Location location = world.getSpawnLocation().clone().add(0.5, 0, 0.5);
        lobby.getPlayers().forEach(p -> p.teleport(location));
        world.getPlayers().forEach(p -> p.teleport(location));
    }

    public void terminate(@Nullable Message message) {
        if (ticker.isRunning()) {
            stop(message);
        }

        delay("Terminating room in %s...", () -> {
            teleportToLobby();
            AzaleaApi.getInstance().getRooms().remove(this);
            Bukkit.unloadWorld(world, false);
            FileUtil.delete(FileUtil.room(name));
        });
    }

    private void delay(String message, Runnable done) {
        AtomicInteger countdown = new AtomicInteger(3);
        ScheduleUtil.doWhile(countdown.get() * 20, 20, () -> {
            String info = String.format(message, countdown.decrementAndGet() + 1);
            broadcaster.broadcast(new ChatMessage(ChatColor.YELLOW + info));
        }, done);
    }
}