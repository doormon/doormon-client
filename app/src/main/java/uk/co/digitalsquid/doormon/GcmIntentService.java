package uk.co.digitalsquid.doormon;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Handles incoming GCM messages
 */
public class GcmIntentService extends IntentService {

    public static final String TAG = "Doormon";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                // TODO: something
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                handleMessage(extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmReceiver.completeWakefulIntent(intent);
    }

    private void handleMessage(Bundle extras) {
        String type = extras.getString("type", "invalid");
        String state = extras.getString("state", "invalid");
        String videoUri = extras.getString("video_uri", "");
        if (type.equals("door")) {
            sendDoorNotification(state, videoUri);
        }
    }

    private void sendDoorNotification(String state, String videoUri) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent innerIntent = null;
        if (videoUri.equals("")) {
            innerIntent = new Intent(this, SetupActivity.class);
        } else {
            innerIntent = new Intent(this, CameraActivity.class);
        }
        innerIntent.setData(Uri.parse(videoUri));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                innerIntent, 0);

        int title_id = R.string.notification_unknown;
        int icon_id = R.drawable.notif_open;
        if (state.equals("open")) {
            title_id = R.string.notification_open;
            icon_id = R.drawable.notif_open;
        } else if (state.equals("closed")) {
            title_id = R.string.notification_closed;
            icon_id = R.drawable.notif_close;
        }


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon_id)
                        .setContentTitle(getResources().getString(title_id));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
