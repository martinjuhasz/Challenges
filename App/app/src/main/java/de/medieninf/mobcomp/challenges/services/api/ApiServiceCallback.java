package de.medieninf.mobcomp.challenges.services.api;

import org.json.JSONObject;

/**
 * Created by Martin Juhasz on 07/06/15.
 */
public abstract class ApiServiceCallback<ReturnType> {

    public void requestFinished(ReturnType returnBody, ApiService.ErrorCode errorCode) {}

}
