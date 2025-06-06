package dev.eposs.elementsutils.api.bosstimer;

import com.google.gson.Gson;
import dev.eposs.elementsutils.ElementsUtils;
import dev.eposs.elementsutils.api.HttpHelper;
import dev.eposs.elementsutils.displays.bosstimer.BossTimerData;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;

public class BossTimerApi {
    private static final URI TIMER_URI = URI.create("https://elements-utils.eposs.dev/api/timer");
    
    public static @Nullable BossTimerData getBossTimerData() {
        try (CloseableHttpResponse response = HttpHelper.request(TIMER_URI)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Status code " + response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
            }
            
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("Response body is empty");
            }
            
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return new Gson().fromJson(bodyAsString, BossTimerData.class);
            
        } catch (Exception e) {
            ElementsUtils.LOGGER.error("Failed to get boss timer data", e);
            return null;
        }
    } 
}
