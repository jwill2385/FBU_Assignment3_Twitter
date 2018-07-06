package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    /**
     * "TimelineActivity"
     */
    private static final String TAG = TimelineActivity.class.getSimpleName();

    private TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;
    // REQUEST_CODE can be any value we like, used to determine the result type later
    private final int REQUEST_CODE = 20;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu() – inflating our menu_main.");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);
        // get id of swipeContainer
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // find the recycler view
        rvTweets = (RecyclerView) findViewById(R.id.rvTweet);

        // initiate arraylist (data source)
        tweets = new ArrayList<>();
        // construct the adapter from this data source
        tweetAdapter = new TweetAdapter(tweets);
        // recyclerView setup (layout manager, use adapter)
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        // set the adapter
        rvTweets.setAdapter(tweetAdapter);
        populateTimeline();

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // config the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    private void populateTimeline() {
        Log.d(TAG, "populateTimeline() – start");

        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "populateTimeline.onSuccess(count = " + response.length() + ")");
                //               Log.d("TwitterClient", response.toString());
                // iterate through JSON array
                // for each entry deserialize the JSON object
                for (int i = 0; i < response.length(); i++) {

                    try {
                        // convert each object to a Tweet model
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        // add that Tweet model to our data source
                        tweets.add(tweet);
                        // notify the adapter that we've added the item
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

        });

    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.

        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "getHomeTimeLine.onSuccess(count = " + response.length() + ")");

                // remember to CLEAR OUT old items before appending in the new ones
                tweetAdapter.clear();
                tweets.clear();
                Log.d(TAG, "count update (count = " + response.length() + ")");
                // iterate through JSON array
                // for each entry deserialize the JSON object
                for (int i = 0; i < response.length(); i++) {
                    try {
                        // convert each object to a Tweet model
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        // add that Tweet model to our data source
                        tweets.add(tweet);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // ...the data has come back, add new items to your adapter...
                tweetAdapter.notifyDataSetChanged();
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }


            // Now we call setRefreshing(false) to signal refresh has finished


            public void onFailure(Throwable e) {
                Log.d("DEBUG", "Fetch timeline error: " + e.toString());
            }

        });

    }
// another way to do an on click. by looking to see if the item i clicked on matches id
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if(item.getItemId() == R.id.miCompose){
//            onComposeAction();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public void onComposeAction(MenuItem mi) {
        Log.d(TAG, "onComposeAction() – start's our compose activity for a result.");

        // this function moves us from TimelineActivity to ComposeActivity
        Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
       // i.putExtra("mode", 2); // pass arbitrary data to launched activity
        startActivityForResult(i, REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");

        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract tweet value from data intent
             Tweet result = Parcels.unwrap(data.getParcelableExtra("tweet"));

             tweets.add(0, result); //adds tweet to top of timeline array
            // notify the adapter that we've added the item
            tweetAdapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0); // scrolls to top

        }
    }

    public void toProfileAction(MenuItem ln){
        Log.d(TAG, "toProfileAction()- sends us to profile page");
        // this function moves us from TimelineActivity to ProfileActivity
        Intent t = new Intent(TimelineActivity.this, ProfileActivity.class);
        startActivity(t);
    }
}
