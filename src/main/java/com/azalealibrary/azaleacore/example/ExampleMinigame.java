package com.azalealibrary.azaleacore.example;

import com.azalealibrary.azaleacore.api.*;
import com.azalealibrary.azaleacore.round.RoundConfiguration;
import com.azalealibrary.azaleacore.round.RoundTeams;
import com.google.common.collect.ImmutableList;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ExampleMinigame extends Minigame {

    // items
    public static final MinigameItem RED_PLAYER_AXE = MinigameItem.create(Material.IRON_AXE, 1)
            .called(ChatColor.DARK_RED + "Red Player Axe")
            .addLore(ChatColor.GRAY + "This the Red team's weapon.")
            .build();
    public static final MinigameItem BLUE_PLAYER_SWORD = MinigameItem.create(Material.IRON_SWORD, 1)
            .called(ChatColor.DARK_BLUE + "Blue Player Axe")
            .addLore(ChatColor.GRAY + "This the Blue team's weapon.")
            .build();

    // teams
    public static final MinigameTeam RED_MINIGAME_TEAM = new MinigameTeam("Red Team", "Kill all blue players.", true, ChatColor.RED, Sound.ENTITY_VILLAGER_AMBIENT, player -> {
        player.setHealth(1);
        player.getInventory().addItem(RED_PLAYER_AXE.getItemStack());
    });
    public static final MinigameTeam BLUE_MINIGAME_TEAM = new MinigameTeam("Blue Team", "Kill all red players.", false, ChatColor.BLUE, Sound.ENTITY_VILLAGER_AMBIENT, player -> {
        player.setHealth(1);
        player.getInventory().addItem(BLUE_PLAYER_SWORD.getItemStack());
    });

    // win conditions
    public static final WinCondition<ExampleRound> NO_BLUE = new WinCondition<>(RED_MINIGAME_TEAM, "No more blue players.", 123, round -> {
        return round.getRoundTeams().getAllInTeam(BLUE_MINIGAME_TEAM).isEmpty();
    });
    public static final WinCondition<ExampleRound> NO_RED = new WinCondition<>(BLUE_MINIGAME_TEAM, "No more red players.", 312, round -> {
        return round.getRoundTeams().getAllInTeam(RED_MINIGAME_TEAM).isEmpty();
    });

    private final MinigameProperty<Location> spawn;

    public ExampleMinigame(World world) {
        this.spawn = MinigameProperty.location("spawn", world.getSpawnLocation()).build();
    }

    public Location getSpawn() {
        return spawn.get(); // example
    }

    @Override
    public String getName() {
        return "ExampleMinigame";
    }

    @Override
    public ImmutableList<WinCondition<?>> getWinConditions() {
        return ImmutableList.of(NO_RED, NO_BLUE);
    }

    @Override
    public ImmutableList<MinigameTeam> getPossibleTeams() {
        return ImmutableList.of(RED_MINIGAME_TEAM, BLUE_MINIGAME_TEAM);
    }

    @Override
    public ExampleRound newRound(RoundConfiguration configuration, List<Player> players) {
        return new ExampleRound(RoundTeams.generate(configuration, new ArrayList<>(getPossibleTeams()), players));
    }

    @Override
    public List<MinigameProperty<?>> getProperties() {
        return List.of(spawn);
    }
}
