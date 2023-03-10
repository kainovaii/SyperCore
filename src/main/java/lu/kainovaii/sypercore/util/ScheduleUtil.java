package lu.kainovaii.sypercore.util;

import lu.kainovaii.sypercore.Main;
import com.google.common.util.concurrent.Runnables;
import org.bukkit.Bukkit;

public final class ScheduleUtil {

    public static int doEvery(int interval, Runnable onInterval) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.INSTANCE, onInterval, 0, interval);
    }

    public static int doDelayed(int delay, Runnable onDone) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(Main.INSTANCE, onDone, delay);
    }

    public static int doWhile(int duration, Runnable onInterval, Runnable onDone) {
        return doWhile(duration, 1, onInterval, onDone);
    }

    public static int doWhile(int duration, int interval, Runnable onInterval) {
        return doWhile(duration, interval, onInterval, Runnables.doNothing());
    }

    public static int doWhile(int duration, int interval, Runnable onInterval, Runnable onDone) {
        int eventId = doEvery(interval, onInterval);
        doDelayed(duration, () -> {
            try {
                onDone.run();
            } catch (Exception exception) {
                System.err.println("Error occurred while running scheduled task: " + exception.getMessage());
                exception.printStackTrace();
            } finally {
                Bukkit.getScheduler().cancelTask(eventId);
            }
        });
        return eventId;
    }
}