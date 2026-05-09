package nemosofts.tamilaudiopro.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.theme.ColorUtils;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.view.EqualizerFragment;

public class EqualizerActivity extends AppCompatActivity {

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int sessionId = PlayerService.getInstance().getAudioSessionID();
        EqualizerFragment equalizerFragment = EqualizerFragment.newBuilder()
                .setAccentColor(ColorUtils.colorPrimary(this))
                .setAudioSessionId(sessionId)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.eqFrame, equalizerFragment)
                .commit();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_equilizer;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}