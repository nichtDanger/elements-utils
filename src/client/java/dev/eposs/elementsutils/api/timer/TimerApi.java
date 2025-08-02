package dev.eposs.elementsutils.api.timer;

import com.google.gson.Gson;
import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public abstract class TimerApi<T> {

    private final Class<T> type;

    private final String apiUrl;

    public TimerApi(Class<T> type, String apiUrl) {
        this.type = type;
        this.apiUrl = apiUrl;
    }

    public T getTimerData() {
        String serverID;
        switch (ModConfig.getConfig().internal.server) {
            case COMMUNITY_SERVER_1 -> serverID = "server1";
            case COMMUNITY_SERVER_2 -> serverID = "server2";
            default -> {
                return null;
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl.replace("$SERVER_ID", serverID)))
                .GET()
                .build();

        try (HttpClient client = HttpClient.newBuilder().build()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Status code " + response.statusCode() + " - " + response.body());
            }

            return new Gson().fromJson(response.body(), type);

        } catch (Exception e) {
            ElementsUtils.LOGGER.error("Failed to get timer data", e);
            return null;
        }
    }
}
