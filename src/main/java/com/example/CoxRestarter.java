package com.example;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
		name = "Squire CoX Auto-Restart",
		description = "Neustartet 'Squire Chambers of Xeric', wenn der Spieler zu lange inaktiv ist",
		tags = {"cox", "afk", "restart", "automation"}
)

public class CoxRestarter extends Plugin {
	@Inject
	private Client client;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SquireCoxAutoRestartConfig config;

	private static final String TARGET_PLUGIN = "Squire Chambers of Xeric";

	private WorldPoint lastPosition;
	private long lastMoveTime;

	@Override
	protected void startUp() {
		log.info("Squire CoX Auto-Restart Plugin gestartet!");
		lastMoveTime = System.currentTimeMillis();
		clientThread.invokeLater(this::monitorPlayerActivity);
	}

	@Override
	protected void shutDown() {
		log.info("Squire CoX Auto-Restart Plugin gestoppt!");
	}

	private void monitorPlayerActivity() {
		while (true) {
			try {
				Thread.sleep(1000); // Prüft alle 1 Sekunde

				if (client.getLocalPlayer() != null) {
					WorldPoint currentPosition = client.getLocalPlayer().getWorldLocation();

					if (lastPosition == null || !lastPosition.equals(currentPosition)) {
						lastPosition = currentPosition;
						lastMoveTime = System.currentTimeMillis();
					}

					// Prüfen, ob der Spieler zu lange inaktiv ist
					int afkTimeMillis = config.inactivityTime() * 1000;
					if (System.currentTimeMillis() - lastMoveTime > afkTimeMillis) {
						log.info("Spieler inaktiv! Neustart von '{}'...", TARGET_PLUGIN);
						restartPlugin(TARGET_PLUGIN);
						lastMoveTime = System.currentTimeMillis(); // Timer zurücksetzen
					}
				}
			} catch (InterruptedException e) {
				log.error("Fehler in der Inaktivitätsüberwachung", e);
			}
		}
	}

	private void restartPlugin(String pluginName) {
		pluginManager.getPlugins().stream()
				.filter(plugin -> plugin.getName().equals(pluginName))
				.findFirst()
				.ifPresent(plugin -> {
					try {
						pluginManager.stopPlugin(plugin);
					} catch (PluginInstantiationException e) {
						throw new RuntimeException(e);
					}
					try {
						Thread.sleep(500); // Kleine Wartezeit für Stabilität
					} catch (InterruptedException ignored) {}
					try {
						pluginManager.startPlugin(plugin);
					} catch (PluginInstantiationException e) {
						throw new RuntimeException(e);
					}
					log.info("'{}' wurde erfolgreich neugestartet!", pluginName);
				});
	}

	@Provides
	SquireCoxAutoRestartConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(SquireCoxAutoRestartConfig.class);
	}
}