package nemosofts.tamilaudiopro.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import com.saimum.saimummusic.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApplicationUtil {

    private ApplicationUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Random RANDOM = new Random();
    public static int getRandomValue(int bound) {
        return RANDOM.nextInt(bound);
    }

    @NonNull
    public static String responsePost(String url, RequestBody requestBody) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(null)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            return "";
        }
    }


    public static int convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static float convertDpToPixel(float dp, @NonNull Resources resources) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * metrics.density;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            InputStream input;
            if(src.contains("https://")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            }
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return null;
        }
    }

    public static GradientDrawable getGradientDrawable(int first, int second) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(15);
        gd.setColors(new int[]{first, second});
        gd.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gd.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
        gd.mutate();
        return gd;
    }

    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        double percentage;
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;
        // return percentage
        return (int) percentage;
    }

    public static String milliSecondsToTimer(long milliseconds, long duration) {
        if (duration > 0) {
            String finalTimerString = "";
            String hourString = "";
            String secondsString = "";
            String minutesString = "";

            // Convert total duration into time
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            // Add hours if there
            int tempHour = (int) (duration / (1000 * 60 * 60));
            if (tempHour != 0) {
                hourString = hours + ":";
            }

            // Prepending 0 to seconds if it is one digit
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }

            // Prepending 0 to minutes if it is one digit
            if (minutes < 10) {
                minutesString = "0" + minutes;
            } else {
                minutesString = "" + minutes;
            }

            finalTimerString = hourString + minutesString + ":" + secondsString;

            return finalTimerString;
        } else {
            return "0:00";
        }
    }

    public static long getSeekFromPercentage(int percentage, long totalDuration) {

        long currentSeconds = 0;
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        currentSeconds = (percentage * totalSeconds) / 100;

        // return percentage
        return currentSeconds * 1000;
    }

    public static String milliSecondsToTimerDownload(long milliseconds) {
        String finalTimerString = "";
        String hourString = "";
        String secondsString = "";
        String minutesString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there

        if (hours != 0) {
            hourString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        // Prepending 0 to minutes if it is one digit
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        finalTimerString = hourString + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static long convertLong(String s) {
        long ms = 0;
        Pattern p = Pattern.compile("(\\d+):(\\d+)");  // Pattern to match "hh:mm"
        Matcher m = p.matcher(s);
        if (m.matches()) {
            int h = Integer.parseInt(m.group(1)); // Extract hours
            int min = Integer.parseInt(m.group(2)); // Extract minutes
            // Convert to milliseconds
            ms = (long) h * 60 * 60 * 1000 + min * 60 * 1000;
        } else {
            Log.d("TimerService", "Invalid time format.");
        }
        return ms;
    }

    public static void log(String tag, String msg) {
        if (BuildConfig.DEBUG){
            Log.e(tag, msg);
        }
    }

    public static void log(String tag, String msg, Exception e) {
        if (BuildConfig.DEBUG){
            Log.e(tag, msg, e);
        }
    }
}
