package dev.eposs.elementsutils.api;

import dev.eposs.elementsutils.ElementsUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;

public class HttpHelper {

    public static CloseableHttpResponse request(URI uri) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            return client.execute(new HttpGet(uri));
        } catch (IOException e) {
            ElementsUtils.LOGGER.error("Failed to request {}", uri, e);
            throw e;
        }
    }

}
