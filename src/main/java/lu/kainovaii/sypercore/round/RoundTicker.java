package lu.kainovaii.sypercore.round;

import lu.kainovaii.sypercore.api.WinCondition;
import lu.kainovaii.sypercore.foundation.message.ChatMessage;
import lu.kainovaii.sypercore.playground.Playground;
import lu.kainovaii.sypercore.util.ScheduleUtil;
import com.google.common.eventbus.EventBus;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.function.Supplier;

public class RoundTicker implements Runnable {

    private final Playground playground;

    private EventBus eventBus;
    private Integer eventId;
    private Round round;
    private int tick;

    public RoundTicker(Playground playground) {
        this.playground = playground;
    }

    public Round getRound() {
        return round;
    }

    public boolean isRunning() {
        return eventId != null;
    }

    public void start(Round newRound) {
        eventBus = new EventBus(playground.getName());
        for (Supplier<Object> listener : playground.getMinigame().getListeners()) {
            eventBus.register(listener.get()); // TODO - review
        }

        eventId = ScheduleUtil.doEvery(1, this);
        round = newRound;
        tick = 0;
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(eventId);
        eventBus.post(new RoundEvent.End(round, tick));
        eventId = null;
        eventBus = null;
    }

    @Override
    public void run() {
        try {
            if (tick == 0) {
                eventBus.post(new RoundEvent.Start(round));
            }

            RoundEvent.Tick tickEvent = new RoundEvent.Tick(round, tick);
            eventBus.post(tickEvent);

            // if tick event has no win condition, check for any other win condition
            WinCondition condition = Optional.ofNullable(tickEvent.getWinCondition())
                    .or(() -> playground.getMinigame().getWinConditions().stream()
                            .filter(c -> c.evaluate(round))
                            .findFirst()).orElse(null);

            if (condition != null) {
                eventBus.post(new RoundEvent.Win(round, tick, condition));
                stop();
            }

            tick++;
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
            playground.stop(ChatMessage.error(exception.getMessage()));
        }
    }
}
