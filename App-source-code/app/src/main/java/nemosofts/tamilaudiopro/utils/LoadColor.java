package nemosofts.tamilaudiopro.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;

import androidx.palette.graphics.Palette;

public class LoadColor extends AsyncTaskExecutor<String, String, String> {

    private static final String TAG = "LoadColor";

    Bitmap bitmap;
    @SuppressLint("StaticFieldLeak")
    View view;

    public LoadColor(View view) {
        this.view = view;
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            bitmap = ApplicationUtil.getBitmapFromURL(strings);
            return "1";
        } catch (Exception e) {
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            Palette.from(bitmap).generate(palette -> {
                if (palette == null){
                    return;
                }

                int defaultValue = 0x000000;
                int vibrant = palette.getVibrantColor(defaultValue);
                try{
                    view.setBackground(ApplicationUtil.getGradientDrawable(vibrant, Color.parseColor("#00000000")));
                } catch (Exception e) {
                    ApplicationUtil.log(TAG,"Error setBackground", e);
                }
            });
        } catch (Exception e) {
            ApplicationUtil.log(TAG,"Error onPostExecute", e);
        }
    }
}
