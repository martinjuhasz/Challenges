package de.medieninf.mobcomp.challenges.services.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import de.medieninf.mobcomp.challenges.R;
import de.medieninf.mobcomp.challenges.services.api.tasks.ApiRequestTaskCallback;
import de.medieninf.mobcomp.challenges.services.api.tasks.SubmitApiRequestTask;
import de.medieninf.mobcomp.challenges.services.api.tasks.SubmitApiRequestTask.RequestType;

/**
 * Created by Martin Juhasz on 07/06/15.
 */
public class ApiService {

    public enum ErrorCode {
        INVALID_PAYLOAD,
        FAILED_REQUEST

    }

    private final String serverUrl;
    private static final String USER_RESSOURCE = "users";


    public ApiService(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void createUser(String username, final ApiServiceCallback<JSONObject> callback) {

        // build url
        String url = serverUrl + "/" + USER_RESSOURCE;

        // build json payload
        JSONObject payloadObject = new JSONObject();
        try {
            payloadObject.put("username", username);
        } catch (JSONException e) {
            // invalid
            callback.requestFinished(null, ErrorCode.INVALID_PAYLOAD);
            return;
        }
        String payload = payloadObject.toString();

        // submit Request
        SubmitApiRequestTask submitTask = new SubmitApiRequestTask(payload, RequestType.POST, new ApiRequestTaskCallback() {
            @Override
            public void requestFinished(String returnBody, boolean successfully) {
                JSONObject returnObject = (JSONObject)parseJSON(returnBody);
                ErrorCode errorCode = getErrorCode(returnObject, successfully);
                callback.requestFinished(returnObject, errorCode);
            }
        });
        submitTask.execute(url);
    }

    private ErrorCode getErrorCode(Object jsonObject, boolean successfully) {
        // web request failed
        if (!successfully) {
            return ErrorCode.FAILED_REQUEST;
        }

        // response body is empty or not an jsonobject
        if (jsonObject == null || !(jsonObject instanceof JSONObject)) {
            return null;
        }

        // jsonobject has no error key
        JSONObject errorObject = (JSONObject)jsonObject;
        if(!errorObject.has("error_code")) {
            return null;
        }

        try {
            int errorCode = errorObject.getInt("error_code");
            // TODO: implement conversion from web error codes into enum values
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    private Object parseJSON(String jsonString) {

        // response body is empty
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }

        // check if json is valid
        Object json = null;
        try {
            json = new JSONTokener(jsonString).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        if (json instanceof JSONArray || json instanceof  JSONObject) {
            return json;
        }
        return null;
    }


}
