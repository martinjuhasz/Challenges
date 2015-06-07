package de.medieninf.mobcomp.challenges.services;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.medieninf.mobcomp.challenges.external.HttpRequest;

/**
 * Created by Martin Juhasz on 06/06/15.
 */
public class SubmitUserTask extends AsyncTask<String, Long, JSONObject> {

    private WebRequestListener listener;
    private String username;

    public SubmitUserTask(String username, WebRequestListener listener) {
        this.username = username;
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", username);

            HttpRequest request = HttpRequest.post(params[0]).send(jsonObject.toString());
            if (request.ok()) {
                String response = request.body();
                JSONObject returnObject = new JSONObject(response);
                return returnObject;
            }

        } catch (JSONException exception) {
            exception.printStackTrace();
            return null;
        } catch (HttpRequest.HttpRequestException exception) {
            exception.printStackTrace();
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (this.listener != null) {
            this.listener.requestFinished(jsonObject);
        }
    }
}
