package org.terraazure.webhookmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import io.leangen.geantyref.TypeToken;

public class Configuration {
  private ConfigurationLoader<CommentedConfigurationNode> configLoader;
  ConfigurationNode rootNode;
  String webhookUrl;
  String ipCheckUrl;
  int externalPort;
  List<String> enabledCommands;
  List<String> enabledEvents;
  String serverName;

  Configuration (ConfigurationLoader<CommentedConfigurationNode> configLoader) {
    this.configLoader = configLoader;
  }

  void load() {
    if (configLoader.canLoad()) {

      ConfigurationNode rootNode;
      try {
        rootNode = configLoader.load();
        webhookUrl = rootNode.node("webhookUrl").getString("https://canary.discordapp.com/api/webhooks");
        ipCheckUrl = rootNode.node("ipCheckUrl").getString("https://api.ipify.org");
        externalPort = rootNode.node("externalPort").getInt(25565);
        serverName = rootNode.node("serverName").getString("Unknown Server Name");

        enabledEvents = getList("enabledEvents", new String[0]);
        enabledCommands = getList("enabledCommands", new String[0]);
      } catch (ConfigurateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public String getWebhookUrl() {
    return this.webhookUrl;
  }

  public String getIPCheckUrl() {
    return this.ipCheckUrl;
  }

  public String getServerName() {
    String name = rootNode.node("serverName").getString();
    if (name == null || name.trim().length() == 0) {
      name = null;
    }
    return name;
  }

  public int getExternalPort() {
    return this.externalPort;
  }

  private List<String> getList(String path, String[] defaultList) {
    try {
      return rootNode.node(path).getList(TypeToken.get(String.class), Arrays.asList(defaultList));
    } catch (SerializationException ex) {
      return new ArrayList<String>();
    }
  }

  public List<String> getEnabledEvents() {
    return this.enabledEvents;
  }

  public boolean isEventEnabled(String eventName) {
    return getEnabledEvents().indexOf(eventName) > -1;
  }

  public List<String> getEnabledCommands() {
    return this.enabledCommands;
  }

  public boolean isCommandEnabled(String command) {
    return getEnabledCommands().indexOf(command) > -1;
  }
}
