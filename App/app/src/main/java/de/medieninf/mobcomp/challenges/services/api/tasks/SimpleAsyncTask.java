package de.medieninf.mobcomp.challenges.services.api.tasks;

import android.os.AsyncTask;

import org.json.JSONException;

import de.medieninf.mobcomp.challenges.external.HttpRequest;
import de.medieninf.mobcomp.challenges.services.api.ApiHandler;
import de.medieninf.mobcomp.challenges.services.api.ApiHandlerCallback;
import de.medieninf.mobcomp.challenges.services.api.ApiHandlerException;

public class SimpleAsyncTask extends AsyncTask<Void, Long, Void>{

    private final ApiHandlerCallback callback;
    private ApiHandler.ErrorCode errorCode;


    public SimpleAsyncTask(ApiHandlerCallback callback) {
        this.callback = callback;
    }

    @Override
    protected final Void doInBackground(Void... params) {
        try {
            doInBackground();
        } catch (ApiHandlerException e) {
            errorCode = e.getErrorCode();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (HttpRequest.HttpRequestException e){
            e.printStackTrace();
            errorCode = ApiHandler.ErrorCode.FAILED_REQUEST;
        }
        return null;
    }

    protected void doInBackground() throws ApiHandlerException, JSONException {
    }

    @Override
    protected final void onPostExecute(Void aVoid) {
        if(this.callback == null) {
            return;
        }
        if (this.errorCode != null) {
            this.callback.requestFailed(this.errorCode);
        } else {
            this.callback.requestFinished();
        }
    }
}
