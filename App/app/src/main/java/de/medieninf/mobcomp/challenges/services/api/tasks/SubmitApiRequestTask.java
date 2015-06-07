package de.medieninf.mobcomp.challenges.services.api.tasks;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import de.medieninf.mobcomp.challenges.external.HttpRequest;
import de.medieninf.mobcomp.challenges.services.api.ApiServiceCallback;

/**
 * Created by Martin Juhasz on 06/06/15.
 */
public class SubmitApiRequestTask extends AsyncTask<String, Long, String> {

    public enum RequestType {
        GET,
        PUT,
        POST,
        DELETE
    }

    private final ApiRequestTaskCallback callback;
    private final RequestType requestType;
    private final String payload;
    private boolean finishedSuccessfully;

    public SubmitApiRequestTask(String payload, RequestType submitType, ApiRequestTaskCallback callback) {
        this.callback = callback;
        this.requestType = submitType;
        this.payload = payload;
        this.finishedSuccessfully = false;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {

            HttpRequest request = null;
            String url = urls[0];

            switch (requestType) {
                case GET:
                    request = HttpRequest.get(url);
                    break;
                case POST:
                    request = HttpRequest.post(url);
                    if (this.payload != null) {
                        request.send(this.payload);
                    }
                    break;
                default:
                    // TODO: implement missing if needed
                    throw new RuntimeException("requestType not implemented");
            }

            // submit
            if (request.ok()) {
                this.finishedSuccessfully = true;

                // extract response json object if needed
                String response = request.body();
                if (response.trim().isEmpty()) {
                    return null;
                }
                return response;
            }
        } catch (HttpRequest.HttpRequestException exception) {
            exception.printStackTrace();
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String returnBody) {
        if (this.callback != null) {
            this.callback.requestFinished(returnBody, this.finishedSuccessfully);
        }
    }
}
