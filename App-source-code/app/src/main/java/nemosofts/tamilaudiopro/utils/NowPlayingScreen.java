package nemosofts.tamilaudiopro.utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.saimum.saimummusic.R;

public enum NowPlayingScreen {

    NORMAL(R.string.player_normal, R.drawable.np_1, 0),
    CIRCLE(R.string.player_circle, R.drawable.np_2, 1),
    FLAT(R.string.player_flat, R.drawable.np_3, 2),
    GRADIENT(R.string.player_gradient, R.drawable.np_4, 3),
    BLUR(R.string.player_blur, R.drawable.np_5, 4),
    CORNER(R.string.player_corner, R.drawable.np_6, 5),
    CORNER_BOTTOM(R.string.player_corner_bottom, R.drawable.np_7, 6);

    @StringRes
    public final int titleRes;
    @DrawableRes
    public final int drawableResId;
    public final int id;

    NowPlayingScreen(@StringRes int titleRes, @DrawableRes int drawableResId, int id) {
        this.titleRes = titleRes;
        this.drawableResId = drawableResId;
        this.id = id;
    }
}
