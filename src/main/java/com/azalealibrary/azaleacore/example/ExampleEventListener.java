package com.azalealibrary.azaleacore.example;

import com.azalealibrary.azaleacore.AzaleaCore;
import com.azalealibrary.azaleacore.api.Round;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ExampleEventListener implements Listener {

    private final Round round;

    public ExampleEventListener(Round round) {
        this.round = round;
    }

    public final void enable() {
        Bukkit.getPluginManager().registerEvents(this, AzaleaCore.INSTANCE);
    }

    public final void disable() {
        HandlerList.unregisterAll(this);
    }

    /* EVENTS... */

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
//        round.getBroadcaster().broadcast(new ChatMessage("Player joined!"));
    }
}
