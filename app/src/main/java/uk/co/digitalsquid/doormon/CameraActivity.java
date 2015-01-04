package uk.co.digitalsquid.doormon;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;

import uk.co.digitalsquid.doormon.mjpeg.MjpegInputStream;
import uk.co.digitalsquid.doormon.mjpeg.MjpegView;


public class CameraActivity extends ActionBarActivity {
    public static final String TAG = "Doormon";

    private MjpegView video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        video = (MjpegView)findViewById(R.id.video);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getIntent().getData() == null) {
            String uri = prefs.getString("camera-uri", null);
            new ConnectTask().execute(uri);
        } else {
            Uri uri = getIntent().getData();
            new ConnectTask().execute(uri.toString());
            prefs.edit().putString("camera-uri", uri.toString()).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        video.startPlayback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        video.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        video.setSource(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ConnectTask extends AsyncTask<String, Void, MjpegInputStream> {
        @Override
        protected void onPostExecute(MjpegInputStream mjpegInputStream) {
            super.onPostExecute(mjpegInputStream);
            video.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            video.setSource(mjpegInputStream);
        }

        @Override
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
                //Error connecting to camera
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
                //Error connecting to camera
            }

            return null;
        }
    }
}
