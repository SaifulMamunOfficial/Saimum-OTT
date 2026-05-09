package nemosofts.tamilaudiopro.utils.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import nemosofts.tamilaudiopro.activity.PlayerService;


public class MediaButtonIntentReceiver extends BroadcastReceiver {

    public MediaButtonIntentReceiver() {
        super();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        String intentAction = intent.getAction();
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN && (PlayerService.getInstance() != null)) {
            Intent intentPause = new Intent(context, PlayerService.class);
            intentPause.setAction(PlayerService.ACTION_TOGGLE);
            context.startService(intentPause);
        }
        abortBroadcast();
    }
}