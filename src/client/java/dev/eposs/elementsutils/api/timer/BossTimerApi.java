package dev.eposs.elementsutils.api.timer;

import dev.eposs.elementsutils.feature.bosstimer.BossTimerData;

public class BossTimerApi extends AbstractTimerApi<BossTimerData> {
    public BossTimerApi() {
        super(BossTimerData.class, "https://elements-utils.eposs.dev/api/bosstimers?server=$SERVER_ID");
    }
}
