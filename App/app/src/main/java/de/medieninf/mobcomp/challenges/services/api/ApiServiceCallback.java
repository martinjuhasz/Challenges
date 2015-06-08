package de.medieninf.mobcomp.challenges.services.api;

/**
 * Created by Martin Juhasz on 07/06/15.
 */
public abstract class ApiServiceCallback {

    public void requestFinished() {}
    public void requestFailed(ApiService.ErrorCode errorCode) {}

}
