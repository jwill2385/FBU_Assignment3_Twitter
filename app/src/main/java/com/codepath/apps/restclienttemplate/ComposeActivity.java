package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {
    private static final String TAG = ComposeActivity.class.getSimpleName();

        public TwitterClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        Log.d(TAG, "onCreate()");
        Button btnTweet = (Button) findViewById(R.id.btnTweet);
        client = TwitterApp.getRestClient(this);

    }

    public void onSubmit(View v) {
        Log.d(TAG, "onSubmit() – sending tweet start.");

        EditText etName = (EditText) findViewById(R.id.etMessage);
        // Pass relevant data back as a result
        //data.putExtra("message", etName.getText().toString());
        client.sendTweet(etName.getText().toString(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "onSubmit.onSuccess(" + response.toString() + ")");
                super.onSuccess(statusCode, headers, response);
                try {
                    Tweet tweet = Tweet.fromJSON(response);
                    Intent data = new Intent();
                    //store tweet information into an intent
                    data.putExtra("tweet", Parcels.wrap(tweet));

                    Log.d(TAG, "onSubmit.onSuccess() – setting activity result to OK");
                    setResult(RESULT_OK, data);

                    Log.d(TAG, "onSubmit.onSuccess() – finishing the activity.");
                    finish(); // closes the activity, pass data to parent
                } catch (JSONException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onSubmit.onFailure()");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d(TAG, "onSubmit.onFailure()");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d(TAG, "onSubmit.onFailure()");
            }
        });
        // Activity finished ok, return the data
    }
}
