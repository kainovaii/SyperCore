package lu.kainovaii.sypercore;

import lu.kainovaii.sypercore.example.Registry;
import lu.kainovaii.sypercore.foundation.AzaleaConfiguration;
import lu.kainovaii.sypercore.foundation.AzaleaEvents;
import lu.kainovaii.sypercore.foundation.message.ChatMessage;
import lu.kainovaii.sypercore.foundation.registry.AzaleaRegistry;
import lu.kainovaii.sypercore.manager.PartyManager;
import lu.kainovaii.sypercore.manager.PlaygroundManager;
import lu.kainovaii.sypercore.manager.TeleporterManager;
import lu.kainovaii.sypercore.util.SerializationUtil;
import lu.kainovaii.sypercore.command.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

import java.io.File;

@SuppressWarnings("unused")
@Plugin(name = "SyperCore", version = Plugin.DEFAULT_VERSION)
@ApiVersion(ApiVersion.Target.v1_13) // compatible with all post-1.13 mc versions
@LogPrefix(Main.PLUGIN_ID)
public final class Main extends JavaPlugin implements Listener {

    public static final String PLUGIN_ID = "SC";
    public static Main INSTANCE;

    public Main() { }

    public Main(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {
        INSTANCE = this;

        CommandNode.register(this, AzaleaCommand.class);
        CommandNode.register(this, GotoCommand.class);
        CommandNode.register(this, PartyCommand.class);
        //CommandNode.register(this, PlaygroundCommand.class);
        CommandNode.register(this, TeleporterCommand.class);
    }

    @Override
    public void onEnable() {
        AzaleaRegistry.EVENT_BUS.register(new Registry()); // TODO - remove

        AzaleaRegistry.MINIGAME.bake();
        AzaleaRegistry.ROUND.bake();
        AzaleaRegistry.ITEM.bake();
        AzaleaRegistry.TEAM.bake();
        AzaleaRegistry.WIN_CONDITION.bake();
        AzaleaRegistry.PROPERTY.bake();
        AzaleaRegistry.COMMAND.bake();

        Bukkit.getPluginManager().registerEvents(new AzaleaEvents(), this);

//        ScheduleUtil.doDelayed(20, () -> {
            SerializationUtil.load("configs", this, AzaleaConfiguration.getInstance());
            SerializationUtil.load("party", this, PartyManager.getInstance());
            //SerializationUtil.load("playground", this, PlaygroundManager.getInstance());
            SerializationUtil.load("teleporters", this, TeleporterManager.getInstance());
//        });
    }

    @Override
    public void onDisable() {
        PlaygroundManager.getInstance().getAll().forEach(playground -> {
            if (playground.hasOngoingRound() && playground.hasParty()) {
                playground.stop(ChatMessage.important(ChatColor.RED + "SyperCore reloaded!"));
            }
        });

        SerializationUtil.save("configs", this, AzaleaConfiguration.getInstance());
        SerializationUtil.save("party", this, PartyManager.getInstance());
        //SerializationUtil.save("playground", this, PlaygroundManager.getInstance());
        SerializationUtil.save("teleporters", this, TeleporterManager.getInstance());
    }
}
