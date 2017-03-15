package webarcade.app;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.*;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import mobisocial.omlet.OmletGameSDK;

public class ArcadeActivity extends Activity {
    private static final String TAG = "Arcade";
    WebView _View;
    private ProgressBar _Progress;
    private ImageButton _Retry;
    private WebViewClient _Client;
    private WebChromeClient _Chrome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_arcade);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        _View = (WebView) findViewById(R.id.web_view);
        _Chrome = new WebChromeClient();
        _Client = new WebViewClient();
        _View.setWebViewClient(_Client);
        _View.setWebChromeClient(_Chrome);
        //_View.getSettings().setSupportMultipleWindows(true);
        _View.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        _View.getSettings().setJavaScriptEnabled(true);
        _View.getSettings().setDomStorageEnabled(true);
        _Progress = (ProgressBar)findViewById(R.id.web_view_progress);
        _Retry = (ImageButton)findViewById(R.id.reload);
        _Retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _Client.clearError();
                _View.reload();
            }
        });

        _View.loadUrl("http://html5games.com");

        OmletGameSDK.setGameChatOverlayEnabled(this, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        OmletGameSDK.onGameActivityResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        OmletGameSDK.onGameActivityPause(this);
    }

    @Override
    public void onBackPressed() {
        if(_View.canGoBack()) {
            _View.clearHistory();
            _View.loadUrl("http://html5games.com");
        } else {
            super.onBackPressed();
        }
    }
    class WebViewClient extends android.webkit.WebViewClient {
        boolean _WasError;
        boolean _Loaded;

        public void clearError() {
            _WasError = false;
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if(_WasError)
                return;
            _Progress.setVisibility(View.VISIBLE);
            if(!_Loaded)
                _View.setVisibility(View.GONE);
            _Retry.setVisibility(View.GONE);
            Log.i(TAG, "Loading " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(_WasError)
                return;
            _Progress.setVisibility(View.GONE);
            _View.setVisibility(View.VISIBLE);
            _Retry.setVisibility(View.GONE);
            _Loaded = true;
        }

        void showReload(String error) {
            _WasError = true;
            Log.i(TAG, "Load failed: " + error);
            _Progress.setVisibility(View.GONE);
            _View.setVisibility(View.GONE);
            _Retry.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            showReload(description);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            showReload(error.toString());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            showReload(error.toString());
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            showReload(errorResponse.toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith("http")) {
                _View.loadUrl(url);
                return true;
            }
            return false;
        }
    }
    class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(_View);
            resultMsg.sendToTarget();
            return true;
        }

    }
}
