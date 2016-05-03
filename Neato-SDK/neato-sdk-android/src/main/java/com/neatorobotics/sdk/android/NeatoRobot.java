package com.neatorobotics.sdk.android;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.widget.Toast;

import com.neatorobotics.sdk.android.constants.NeatoError;
import com.neatorobotics.sdk.android.models.Robot;
import com.neatorobotics.sdk.android.nucleo.NucleoBaseClient;
import com.neatorobotics.sdk.android.nucleo.NucleoResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Marco on 11/03/16.
 */
public class NeatoRobot{

    private static final String TAG = "NeatoRobot";

    private Context context;
    //the serializable model
    private Robot robot;

    private String baseUrl;
    @VisibleForTesting
    protected AsyncCall asyncCall;

    //constructor
    public NeatoRobot(Context context, Robot robot){
        this.context = context;
        this.baseUrl = context.getString(R.string.nucleo_endpoint);
        this.asyncCall = new AsyncCall();
        this.robot = robot;
    }

    private void setRobotState(JSONObject json) {
        //robot.setRobotState();
    }

    /**
     * Retrieve the user robots list.
     * @param callback
     */
    public void updateRobotState(final NeatoCallback<Boolean> callback) {
        String GET_ROBOT_STATE_COMMAND = "{\"reqId\": \"77\",\"cmd\": \"getRobotState\"}";
        try {
            JSONObject command = new JSONObject(GET_ROBOT_STATE_COMMAND);
            asyncCall.executeCall(this,context, baseUrl, robot.serial,command, robot.secretKey, new NeatoCallback<NucleoResponse>(){
                @Override
                public void done(NucleoResponse result) {
                    super.done(result);
                    if(result == null) {
                        callback.fail(NeatoError.GENERIC_ERROR);
                    }else if(result.isHttpOK()) {
                        callback.done(true);
                        Toast.makeText(context,result.getJSON().toString(),Toast.LENGTH_SHORT).show();
                    }else {
                        callback.fail(NeatoError.GENERIC_ERROR);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG,"Exception",e);
            callback.fail(NeatoError.INVALID_JSON);
        }
    }

    //region serialization
    public Object serialize() {
        return robot;
    }

    public static NeatoRobot deserialize(Context context, Object serializable) {
        return new NeatoRobot(context,(Robot) serializable);
    }
    //endregion

    //region getters and setters
    public String getName() {
        return robot.name;
    }

    public String getSerial() {
        return robot.serial;
    }

    public String getSecretKey() {
        return robot.secretKey;
    }

    public String getModel() {
        return robot.model;
    }

    public String getLinkedAt() {
        return robot.linkedAt;
    }
    //endregion

    //region async call
    private class AsyncCall {

        private static final String TAG = "AsyncCall";

        public void executeCall(final NeatoRobot neatoRobot, final Context context, final String url, final String robot_serial, final JSONObject command, final String robotSecretKey, final NeatoCallback<NucleoResponse> callback) {
            final AsyncTask<Void, Void, NucleoResponse> task = new AsyncTask<Void, Void, NucleoResponse>() {
                protected void onPreExecute() {}
                protected NucleoResponse doInBackground(Void... unused) {
                    return NucleoBaseClient.executeNucleoCall(context,url,robot_serial,command,robotSecretKey);
                }
                protected void onPostExecute(NucleoResponse response) {
                    if(response != null && response.isStateResponse()) neatoRobot.setRobotState(response.getJSON());
                    callback.done(response);
                }
            };
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else {
                task.execute();
            }
        }
    }
    //endregion
}