package com.azalealibrary.azaleacore.round;

import com.azalealibrary.azaleacore.api.Round;
import com.azalealibrary.azaleacore.api.WinCondition;
import com.azalealibrary.azaleacore.room.MinigameRoom;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.function.Consumer;

public class RoundTicker implements Runnable {

    private final MinigameRoom room;
    private final RoundConfiguration configuration;

    private Round round;
    private Integer eventId;
    private int graceCountdown = -1;

    public RoundTicker(MinigameRoom room, RoundConfiguration configuration) {
        this.room = room;
        this.configuration = configuration;
    }

    public boolean isRunning() {
        return eventId != null;
    }

    public void begin(Round newRound) {
        eventId = Bukkit.getScheduler().scheduleSyncRepeatingTask(configuration.getPlugin(), this, 0L, configuration.getTickRate());
        round = newRound;
        graceCountdown = -1;
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(eventId);
        eventId = null;
    }

    @Override
    public void run() {
        if (graceCountdown == -1) {
            round.onSetup(new RoundEvent.Setup(room));
            graceCountdown++;
        } else if (graceCountdown < configuration.getGraceTickDuration()) {
            graceCountdown++;
        } else if (graceCountdown == configuration.getGraceTickDuration()) {
            if (round.getTick() == 0) {
                round.onStart(new RoundEvent.Start(room));
                round.setTick(round.getTick() + 1);
            } else if (round.getTick() < configuration.getRoundTickDuration()) {
                RoundEvent.Tick tickEvent = new RoundEvent.Tick(room);
                round.onTick(tickEvent);
                round.setTick(round.getTick() + 1);

                Optional.ofNullable(tickEvent.getCondition())
                        .ifPresent(w -> handleWinCondition(round::onWin, new RoundEvent.Win(w, room)));
                room.getMinigame().getWinConditions().stream()
                        .filter(c -> ((WinCondition<Round>) c).evaluate(round))
                        .findFirst()
                        .ifPresent(w -> handleWinCondition(round::onWin, new RoundEvent.Win(w, room)));
            } else if (round.getTick() == configuration.getRoundTickDuration()) {
                handleWinCondition(round::onEnd, new RoundEvent.End(room));
            }
        }
    }

    private <E extends RoundEvent.End> void handleWinCondition(Consumer<E> dispatcher, E event) {
        dispatcher.accept(event);

        if (event.doRestart()) {
            round.setTick(0);
        } else {
            cancel();

            if (!(dispatcher instanceof RoundEvent.End)) {
                round.onEnd(event); // also call RoundLifeCycle#onEnd too when #onWin is called
            }
        }
    }
}