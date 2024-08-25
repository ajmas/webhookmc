package org.terraazure.webhookmc;

import java.io.Reader;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.world.Location;

import com.google.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebhookMCUtils {
    private static WebhookMCUtils instance;
    private Configuration config;
    private Logger logger;

    @Inject
    private void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public String getLocationAsString(Living player, boolean includeWorld) {
        if (player != null) {
            Location<?, ?> location = player.location();

            String message = String.format(
                    "%d %d %d",
                    location.blockX(),
                    location.blockY(),
                    location.blockZ());

            if (includeWorld) {
                message += String.format(
                        " (%s@%s)",
                        location.world().properties().toString(),
                        config.getServerName());
            }

            return message.toString();
        }
        return null;
    }

    public String getExternalIP(String ipCheckUrl) {
        String address = null;
        Response response = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(ipCheckUrl)
                    .get()
                    .build();

            response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                StringBuilder strBuilder = new StringBuilder();
                char[] buffer = new char[128];
                Reader reader = response.body().charStream();
                int len = -1;
                while ((len = reader.read(buffer)) > -1) {
                    strBuilder.append(buffer, 0, len);
                }
                address = strBuilder.toString();
            }
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return address;
    }

    public static WebhookMCUtils getInstance() {
        if (instance == null) {
            instance = new WebhookMCUtils();
        }
        return instance;
    }
}
