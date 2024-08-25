package org.terraazure.webhookmc.senders;

import java.io.IOException;

import org.json.JSONObject;
import org.terraazure.webhookmc.Configuration;

import com.google.inject.Inject;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import org.apache.logging.log4j.Logger;

public class DiscordWebhookSender implements WebhookSender {
  private String discordUrl;
  private Logger logger;

  @Inject
  private void setLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void sendMessage(String message) {
    if (this.discordUrl == null) {
      return;
    }

    Response response = null;
    try {
      JSONObject obj = new JSONObject().put("content", message);

      OkHttpClient client = new OkHttpClient();
      RequestBody body = RequestBody.create(obj.toString(), MediaType.parse("application/json; charset=utf-8"));
      Request request = new Request.Builder()
          .url(discordUrl)
          .post(body)
          .build();

      response = client.newCall(request).execute();
    } catch (IOException exception) {
      logger.error(exception);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  @Override
  public void setConfig(Configuration config) {
    // not implemented
  }

}
