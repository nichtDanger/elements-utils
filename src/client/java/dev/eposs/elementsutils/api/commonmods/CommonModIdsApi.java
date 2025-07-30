package dev.eposs.elementsutils.api.commonmods;

import com.google.gson.Gson;
import dev.eposs.elementsutils.ElementsUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class CommonModIdsApi {
	private static final String COMMON_MOD_IDS_URI = "https://elements-utils.eposs.dev/api/common_mod_ids";
	private static List<String> cachedCommonModIds = Collections.emptyList();

	public static void fetchCommonModIds() {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(COMMON_MOD_IDS_URI))
				.GET()
				.build();

		try (HttpClient client = HttpClient.newBuilder().build()) {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				CommonModIdsResponse result = new Gson().fromJson(response.body(), CommonModIdsResponse.class);
				cachedCommonModIds = result.common_mod_ids != null ? result.common_mod_ids : Collections.emptyList();
			} else {
				ElementsUtils.LOGGER.error("Failed to fetch common mod ids: {}", response.statusCode());
			}
		} catch (Exception e) {
			ElementsUtils.LOGGER.error("Failed to fetch common mod ids", e);
		}
	}

	public static List<String> getCachedCommonModIds() {
		return cachedCommonModIds;
	}

	private static class CommonModIdsResponse {
		public List<String> common_mod_ids;
	}
}
