package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.Objects;

import com.saimum.saimummusic.R;


public class NewsWebActivity extends NSoftsPlayerActivity {

    private String url;
    private TextView titleView;
    private TextView textViewUrl;
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent()!=null){
            url = getIntent().getStringExtra("URL");
        }

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_web_news, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.GONE);
        adViewPlayer.setVisibility(View.VISIBLE);

        toolbar.setVisibility(View.GONE);

        Toolbar toolbarNews = findViewById(R.id.webviewToolbar);
        setSupportActionBar(toolbarNews);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        toolbarNews.setNavigationOnClickListener(view -> goBack());

        titleView = findViewById(R.id.webviewTitle);
        textViewUrl = findViewById(R.id.webviewUrl);
        textViewUrl.setText(url);

        final ProgressBar progressBar = findViewById(R.id.pb_web);
        webView = findViewById(R.id.wv_web);
        webView.setWebViewClient(new MyWebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (!TextUtils.isEmpty(title)) {
                    titleView.setText(title);
                    titleView.setSelected(true);
                }
            }

        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(url);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
            url = request.getUrl().toString();
            textViewUrl.setText(url);
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_close, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_close) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBack() {
        if (webView != null){
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}