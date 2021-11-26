package de.melanx.easyskyblockmanagement.client.screen.base;

import de.melanx.easyskyblockmanagement.client.widget.LoadingCircle;
import de.melanx.easyskyblockmanagement.util.LoadingResult;

public interface LoadingResultHandler {

    LoadingCircle createLoadingCircle();

    void onLoadingResult(LoadingResult result);
}
