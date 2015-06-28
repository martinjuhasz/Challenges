package de.medieninf.mobcomp.challenges.services.api.tasks;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import de.medieninf.mobcomp.challenges.external.HttpRequest;
import de.medieninf.mobcomp.challenges.services.api.ApiHandler;
import de.medieninf.mobcomp.challenges.services.api.ApiHandler.ErrorCode;
import de.medieninf.mobcomp.challenges.services.api.ApiHandlerCallback;

/**
 * Created by Martin Juhasz on 08/06/15.
 */
public abstract class ApiHandlerAsyncTask extends AsyncTask<Void, Long, Void> {

    private final ApiHandlerCallback callback;
    private ErrorCode errorCode;

    public ApiHandlerAsyncTask(ApiHandlerCallback callback) {
        this.callback = callback;
        this.errorCode = null;
    }

    protected abstract HttpRequest onPrepareRequest();

    protected boolean onDataReceived(JSONObject returnObject) {
        return true;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            HttpRequest request = onPrepareRequest();
            if (request != null && request.ok()) {

                // extract response json object if needed
                String response = request.body();
                if (response.trim().isEmpty()) {
                    return null;
                }

                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject == null) {
                    return null;
                }

                this.errorCode = getErrorCode(jsonObject);
                if(this.errorCode == null) {
                    boolean received = onDataReceived(jsonObject);
                    if (!received) {
                        this.errorCode = ErrorCode.UNKNOWN;
                    }
                }
                return null;
            } else {
                this.errorCode = ErrorCode.FAILED_REQUEST;
                return null;
            }

        } catch (HttpRequest.HttpRequestException exception) {
            exception.printStackTrace();
            this.errorCode = ErrorCode.FAILED_REQUEST;
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(this.callback == null) {
            return;
        }
        if (this.errorCode != null) {
            this.callback.requestFailed(this.errorCode);
        } else {
            this.callback.requestFinished();
        }
    }

    private ErrorCode getErrorCode(JSONObject jsonObject) {
        // response body is empty or not an jsonobject
        if (jsonObject == null) {
            return null;
        }

        if(!jsonObject.has("error_code")) {
            return null;
        }

        try {
            int errorCode = jsonObject.getInt("error_code");
            // TODO: implement conversion from web error codes into enum values
            return null;
        } catch (JSONException e) {
            return null;
        }
    }
}
