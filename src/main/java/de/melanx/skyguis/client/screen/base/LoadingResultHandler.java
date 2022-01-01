package de.melanx.skyguis.client.screen.base;

import de.melanx.skyguis.client.widget.LoadingCircle;
import de.melanx.skyguis.util.LoadingResult;

public interface LoadingResultHandler {

    LoadingCircle createLoadingCircle();

    void onLoadingResult(LoadingResult result);
}
