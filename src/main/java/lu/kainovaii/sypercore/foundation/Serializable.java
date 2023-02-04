package lu.kainovaii.sypercore.foundation;

import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;

public interface Serializable {

    void serialize(@Nonnull ConfigurationSection configuration);

    void deserialize(@Nonnull ConfigurationSection configuration);
}
