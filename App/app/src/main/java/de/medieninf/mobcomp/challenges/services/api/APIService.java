package de.medieninf.mobcomp.challenges.services.api;

import org.json.JSONException;
import org.json.JSONObject;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.services.api.tasks.SubmitApiRequestTask;
import de.medieninf.mobcomp.challenges.services.api.tasks.SubmitApiRequestTask.RequestType;

/**
 * Created by Martin Juhasz on 07/06/15.
 */
public class ApiService {

    public enum ErrorCode {
        INVALID_PAYLOAD,

    }

    private final String serverUrl;
    private static final String USER_RESSOURCE = "users";


    public ApiService(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void createUser(String username, ApiServiceCallback callback) {

        // build url
        String url = serverUrl + "/" + USER_RESSOURCE;

        // build json payload
        JSONObject payloadObject = new JSONObject();
        try {
            payloadObject.put("username", username);
        } catch (JSONException e) {
            // invalid
            callback.requestFinished(null, false, ErrorCode.INVALID_PAYLOAD);
            return;
        }
        String payload = payloadObject.toString();

        // submit Request
        SubmitApiRequestTask submitTask = new SubmitApiRequestTask(payload, RequestType.POST, callback);
        submitTask.execute(url);
    }


}
