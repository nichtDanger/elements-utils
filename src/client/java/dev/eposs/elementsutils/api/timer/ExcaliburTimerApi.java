package dev.eposs.elementsutils.api.timer;

import dev.eposs.elementsutils.feature.excaliburtimer.ExcaliburTimerData;

public class ExcaliburTimerApi extends AbstractTimerApi<ExcaliburTimerData> {
    public ExcaliburTimerApi() {
        super(ExcaliburTimerData.class, "https://elements-utils.eposs.dev/api/excalibur?server=$SERVER_ID");
    }
}
