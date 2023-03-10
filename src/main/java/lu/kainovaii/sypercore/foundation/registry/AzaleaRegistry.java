package lu.kainovaii.sypercore.foundation.registry;

import lu.kainovaii.sypercore.api.MinigameItem;
import lu.kainovaii.sypercore.api.MinigameTeam;
import lu.kainovaii.sypercore.api.WinCondition;
import lu.kainovaii.sypercore.command.CommandNode;
import lu.kainovaii.sypercore.foundation.configuration.property.ConfigurableProperty;
import lu.kainovaii.sypercore.minigame.MinigameIdentifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class AzaleaRegistry<T> {

    public static final EventBus EVENT_BUS = new EventBus("registry");

    public static final AzaleaRegistry<MinigameIdentifier> MINIGAME = new AzaleaRegistry<>("minigame", RegistryEvent.Minigames::new);
    public static final AzaleaRegistry<Supplier<Object>> ROUND = new AzaleaRegistry<>("round", RegistryEvent.Rounds::new);
    public static final AzaleaRegistry<MinigameItem<?>> ITEM = new AzaleaRegistry<>("item", RegistryEvent.Items::new);
    public static final AzaleaRegistry<MinigameTeam> TEAM = new AzaleaRegistry<>("team", RegistryEvent.Teams::new);
    public static final AzaleaRegistry<WinCondition> WIN_CONDITION = new AzaleaRegistry<>("win condition", RegistryEvent.WinConditions::new);
    public static final AzaleaRegistry<Supplier<ConfigurableProperty<?>>> PROPERTY = new AzaleaRegistry<>("property", RegistryEvent.Properties::new);
    public static final AzaleaRegistry<CommandNode> COMMAND = new AzaleaRegistry<>("command", RegistryEvent.Commands::new);

    private final String name;
    private final Supplier<?> event;

    private ImmutableMap<MinigameIdentifier.Tag, T> objects;

    public AzaleaRegistry(String name, Supplier<?> event) {
        this.name = name;
        this.event = event;
    }

    public ImmutableList<T> getObjects() {
        return objects.values().asList();
    }

    public ImmutableMap<MinigameIdentifier.Tag, T> getEntries() {
        return objects;
    }

    public T get(MinigameIdentifier.Tag tag) {
        return objects.get(tag);
    }

    public List<T> getAll(MinigameIdentifier identifier) {
        return getEntries().entrySet().stream()
                .filter(entry -> entry.getKey().getIdentifier().equals(identifier))
                .map(Map.Entry::getValue)
                .toList();
    }

    public <E extends RegistryEvent<T>> void bake() {
        if (objects != null) {
            throw new RuntimeException("Attempting to re-bake already baked " + name + " registry.");
        }

        E registryEvent = (E) event.get();
        AzaleaRegistry.EVENT_BUS.post(registryEvent);
        objects = ImmutableMap.copyOf(registryEvent.getObjects());
    }
}
