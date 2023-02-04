package lu.kainovaii.sypercore.example.teams;

import lu.kainovaii.sypercore.api.MinigameTeam;
import lu.kainovaii.sypercore.example.Registry;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class RedTeam extends MinigameTeam {

    public RedTeam() {
        super("Red Team", "Kill all blue players.", ChatColor.RED, Sound.ENTITY_VILLAGER_AMBIENT);
    }

    @Override
    public void prepare(Player player) {
        player.getInventory().clear();
        player.getInventory().addItem(Registry.RED_PLAYER_AXE.getItemStack());
    }
}
