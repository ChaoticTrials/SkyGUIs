package de.melanx.skyguis.client.screen.base;

import de.melanx.skyguis.client.screen.BaseScreen;
import de.melanx.skyguis.client.widget.LoadingCircle;
import de.melanx.skyguis.util.LoadingResult;

public interface LoadingResultHandler {

    default LoadingCircle createLoadingCircle(BaseScreen screen) {
        return new LoadingCircle(screen.centeredX(32), screen.centeredY(32), 32);
    }

    // todo 1.21 simplify
    void onLoadingResult(LoadingResult result);
}
