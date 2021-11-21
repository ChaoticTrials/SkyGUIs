package de.melanx.easyskyblockmanagement.network;

import net.minecraft.network.chat.Component;

public class LoadingResult {

    private Status status;
    private Component reason;

    public LoadingResult(Status status, Component reason) {
        this.status = status;
        this.reason = reason;
    }

    public LoadingResult.Status getStatus() {
        return this.status;
    }

    public Component getReason() {
        return this.reason;
    }

    public enum Status {
        SUCCESS,
        FAIL
    }
}
