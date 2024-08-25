package org.terraazure.webhookmc.senders;

import org.terraazure.webhookmc.Configuration;

public interface WebhookSender {
  void sendMessage(String message);
  void setConfig(Configuration config);
}
