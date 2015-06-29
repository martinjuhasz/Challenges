package de.medieninf.mobcomp.challenges.services.api.tasks;

import android.os.AsyncTask;

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
        }
        return null;
    }

    protected void doInBackground() throws ApiHandlerException{
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
