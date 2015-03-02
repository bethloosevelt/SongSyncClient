package com.example.dalewesa.listeningroom;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.playback.Config;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

import org.json.*;


public class MainActivity extends Activity
        implements PlayerNotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "2e43900fe9e84ce6a7822c13d3ea3472";
    private static final String REDIRECT_URI = "song.sync://callback";

    private Player mPlayer;
    private static final int REQUEST_CODE = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button seekButton = (Button) findViewById(R.id.seek_button);
        final EditText seekTime = (EditText) findViewById(R.id.track_seek);

        final Button searchButton = (Button) findViewById(R.id.search_button);
        final EditText searchText = (EditText) findViewById(R.id.search_text);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        /////////////////////////////////////
        // MESSY PILE OF ONCLICK LISTENERS //
        /////////////////////////////////////

        // Set up button that seeks to new part in track
        seekButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int time = Integer.parseInt(seekTime.getText().toString());
                time = time * 1000; // secs to millisex (haha)

                mPlayer.seekToPosition(time);

                // Clear text
                seekTime.setText("");
            }
        });

        // Set up button that searches for tracks
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PopulateSearchTask task = new PopulateSearchTask();
                task.execute(searchText.getText().toString());
            }
        });
    }

    class PopulateSearchTask extends AsyncTask<String, Void, String> {

        /**
         * Let's the user know we're downloading the contacts
         * from the endpoint.
         */
        @Override
        protected void onPreExecute() {
            Log.d("MainActivity", "Fetching tracks...");
        }

        @Override
        protected String doInBackground(String... trackNames) {
            // We only need one url despite having varargs
            String trackName = trackNames[0];

            // Format API request url
            String url = "https://api.spotify.com/v1/search?q=" + trackName + "&type=track";

            try {
                return NetworkUtils.getJson(url);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String json) {
            populateSearchResults(json);
        }
    }

    private void populateSearchResults(String json) {
        try {
            JSONObject parsed = new JSONObject(json);

            JSONArray tracks = parsed.getJSONObject("tracks").getJSONArray("items");

            ArrayList<String> songTitles = new ArrayList<>();
            for (int i = 0; i < tracks.length(); i++) {
                String name = tracks.getJSONObject(i).getString("name");
                Log.d("MainActivity", name);
                songTitles.add(name);
            }

            ArrayAdapter<String> mResultsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songTitles);

            ListView results = (ListView) findViewById(R.id.results_list);
            results.setAdapter(mResultsAdapter);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                        mPlayer.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " +
                               throwable.getMessage());
                    }
                });
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
