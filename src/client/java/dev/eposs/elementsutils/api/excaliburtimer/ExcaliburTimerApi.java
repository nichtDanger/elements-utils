package dev.eposs.elementsutils.api.excaliburtimer;

import com.google.gson.Gson;
import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.config.ModConfig;
import dev.eposs.elementsutils.feature.excaliburtimer.ExcaliburTimerData;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExcaliburTimerApi {
	private static final String TIMER_URI = "https://elements-utils.eposs.dev/api/excalibur?server=$SERVER_ID";

	public static @Nullable ExcaliburTimerData getExcaliburTimerData() {
		String serverID;
		switch (ModConfig.getConfig().internal.server) {
			case COMMUNITY_SERVER_1 -> serverID = "server1";
			case COMMUNITY_SERVER_2 -> serverID = "server2";
			default -> {
				return null;
			}
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(TIMER_URI.replace("$SERVER_ID", serverID)))
				.GET()
				.build();

		try (HttpClient client = HttpClient.newBuilder().build()) {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new IOException("Status code " + response.statusCode() + " - " + response.body());
			}

			return new Gson().fromJson(response.body(), ExcaliburTimerData.class);

		} catch (Exception e) {
			ElementsUtils.LOGGER.error("Failed to get excalibur timer data", e);
			return null;
		}
	}
}
