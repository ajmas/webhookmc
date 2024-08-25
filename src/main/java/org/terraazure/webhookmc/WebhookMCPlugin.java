/*
 * Copyright (C) 2024 Andre-John Mas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.terraazure.webhookmc;

import org.terraazure.webhookmc.senders.WebhookSender;

import com.google.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command.Raw;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent.Death;
import net.kyori.adventure.text.Component;

import org.spongepowered.api.event.network.ServerSideConnectionEvent.Join;
import org.spongepowered.api.event.network.ServerSideConnectionEvent.Leave;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;

// see: https://github.com/Azuriom/AzLink/blob/master/sponge/src/main/java/com/azuriom/azlink/sponge/AzLinkSpongePlugin.java
@SuppressWarnings("WeakerAccess")
@Plugin("webhookmc")
public class WebhookMCPlugin implements Runnable {
  static final int DEFAULT_PORT = 25565;
  static final String PLUGIN_NAME = "webhookmc";

  private boolean enabled = true;
  private String externalIP = "";
  private long taskInterval = 900000;

  private WebhookMCUtils webhookMCUtils;
  private WebhookSender webhookSender;
  @Inject
  private Logger logger;
  @Inject
  @DefaultConfig(sharedRoot = false)
  private ConfigurationLoader<CommentedConfigurationNode> configLoader;
  private Configuration config;

  public WebhookMCPlugin() {
    // this.config = config;
    // this.discordMcUtils = DiscordMcUtils.getInstance();
  }

  private String getServerName() {
    return config.getServerName();
  };

  private String getServerVersion() {
    return "0.0.0";
  };

  @Listener
  public void onServerStarted(StartedEngineEvent<Server> event) {
    String serverName = getServerName();
    Server server = Sponge.server();
    Platform platform = Sponge.platform();
    int maxPlayers = server.maxPlayers();
    String serverVersion = platform.minecraftVersion().toString();

    this.webhookSender.sendMessage("The Minecraft server  **"
        + serverName + "** is online! Running **" + serverVersion
        + "**. Max players: **" + maxPlayers + "**");
  }

  @Listener
  public void onServerStop(StoppingEngineEvent<Server> event) {
    String serverName = getServerName();

    this.webhookSender.sendMessage("The MC server **"
        + serverName + "** is going offline!");
  }

  @Listener
  public void onRefresh(RefreshGameEvent event) {
    config = new Configuration(configLoader);
    config.load();
    webhookMCUtils = new WebhookMCUtils();
    webhookMCUtils.setConfig(config);
  }

  @Listener
  public void onRegisterCommands(RegisterCommandEvent<Raw> event) {
    // event.register
    // TODO
    // event.register(this.pluginContainer, new SpongeCommandExecutor(this.plugin),
    // "azlink", "azuriomlink");
  }

  @Listener
  public void onPlayerJoin(Join event) {
    if (this.config.getEnabledEvents().indexOf("playerJoin") > -1) {
      logger.info("A player joined the server! Sending an update to Discord...");

      String serverName = getServerName();
      String playerName = event.player().name();
      Server server = Sponge.server();
      int maxPlayers = server.maxPlayers();
      int playerCount = server.onlinePlayers().size();

      event.player().sendMessage(Component.text("Welcome " + playerName));

      if (serverName != null) {
        webhookSender.sendMessage("**" + playerName
            + "** Joined the MC server! **"
            + serverName + "**. Online count: **" + playerCount
            + "/" + maxPlayers + "**");
      } else {
        webhookSender.sendMessage("**"
            + playerName + "** Joined the server! Online count: **"
            + playerCount + "/" + maxPlayers + "**");
      }
    }
  }

  @Listener
  public void onPlayerQuit(Leave event) {
    if (this.config.getEnabledEvents().indexOf("playerQuit") > -1) {
      String serverName = getServerName();
      String playerName = event.player().name();
      Server server = Sponge.server();
      int maxPlayers = server.maxPlayers();
      int playerCount = server.onlinePlayers().size();

      // event.player().displayName();
      logger.info("A player left the server! Sending an update to Discord...");

      if (serverName != null) {
        webhookSender.sendMessage("**" + playerName + "** Left the MC server **"
            + serverName + "**! Online count: **" + playerCount + "/"
            + maxPlayers + "**");
      } else {
        webhookSender.sendMessage("**" + playerName
            + "** Left the server! Online count: **" + playerCount + "/"
            + maxPlayers + "**");
      }
    }
  }

  @Listener
  public void onPlayerDeath(Death event) {
    if (this.config.getEnabledEvents().indexOf("playeDeath") > -1) {
      String location = this.webhookMCUtils.getLocationAsString(event.entity(), true);
      String message = event.message().toString();
      Living livingEntity = event.entity();

      if (livingEntity instanceof Player) {
        Player player = (Player) livingEntity;
        webhookSender.sendMessage(player.name() + "died: " + message + " (location: " + location + ")");
      }
    }
  }

  public void onExternalAddressChange(String address) {
    String name = getServerName();
    String serverVersion = getServerVersion();
    if (name != null) {
      webhookSender.sendMessage("MC server, **" + name
          + "** (" + serverVersion + "), address is **" + address + "**");
    } else {
      webhookSender.sendMessage("MC server ("
          + serverVersion + ") address is **" + address + "**");
    }
  }

  @Override
  public void run() {
    String ipCheckUrl = config.getIPCheckUrl();
    int serverPort = config.getExternalPort();

    while (this.enabled) {
      try {

        String newExternalIP = webhookMCUtils.getExternalIP(ipCheckUrl);
        if (!newExternalIP.equals(externalIP)) {
          this.externalIP = newExternalIP;
          logger.info("External IP: " + this.externalIP);

          String address = this.externalIP;
          if (serverPort != DEFAULT_PORT) {
            address += ":" + serverPort;
          }

          this.onExternalAddressChange(address);
        }

        // sleep for 15 minutes
        Thread.sleep(this.taskInterval);
      } catch (InterruptedException ex) {
        // ignored
      }
    }
    logger.info("Stopping periodic Discord Webhook tasks");
  }
}
