package lu.kainovaii.sypercore.example.winconditions;

import lu.kainovaii.sypercore.api.WinCondition;
import lu.kainovaii.sypercore.example.Registry;
import lu.kainovaii.sypercore.round.Round;

public class NoBluePlayers extends WinCondition {

    public NoBluePlayers() {
        super("All blue players have been eliminated", 100, Registry.RED_TEAM);
    }

    @Override
    public boolean evaluate(Round round) {
        return round.getTeams().getAllInTeam(Registry.BLUE_TEAM).isEmpty();
    }
}
