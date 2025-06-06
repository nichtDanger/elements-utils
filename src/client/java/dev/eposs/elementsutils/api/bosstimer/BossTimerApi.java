package dev.eposs.elementsutils.api.bosstimer;

import com.google.gson.Gson;
import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.displays.bosstimer.BossTimerData;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BossTimerApi {
    private static final URI TIMER_URI = URI.create("https://elements-utils.eposs.dev/api/bosstimers");

    public static @Nullable BossTimerData getBossTimerData() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(TIMER_URI)
                .GET()
                .build();

        try (HttpClient client = HttpClient.newBuilder().build()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Status code " + response.statusCode() + " - " + response.body());
            }

            return new Gson().fromJson(response.body(), BossTimerData.class);

        } catch (Exception e) {
            ElementsUtils.LOGGER.error("Failed to get boss timer data", e);
            return null;
        }
    }
}
