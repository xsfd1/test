package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("squirecoxautorestart")
public interface SquireCoxAutoRestartConfig extends Config {

	@ConfigItem(
			keyName = "inactivityTime",
			name = "Inaktivitätszeit (Sekunden)",
			description = "Zeit in Sekunden, bevor 'Squire Chambers of Xeric' neugestartet wird"
	)
	default int inactivityTime() {
		return 60; // Standard: 1 Minute
	}
}