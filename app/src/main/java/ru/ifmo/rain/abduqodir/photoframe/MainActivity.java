package ru.ifmo.rain.abduqodir.photoframe;

import android.graphics.Bitmap;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.yandex.disk.client.Credentials;

import ru.ifmo.rain.abduqodir.photoframe.directory_content.DirectoryContentActivity;

public class MainActivity extends AppCompatActivity {

  public static final String CLIENT_ID = "984264711da54bf7b0cb21402ab1d61a";
  public static final String CALLBACK_URL = "myapp://token";
  public static final String AUTHORISATION_REQUEST = "https://oauth.yandex.ru/authorize?"
      + "response_type=token&client_id=" + CLIENT_ID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    WebView webView = (WebView) findViewById(R.id.webView);
    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

    webView.getSettings().setLoadWithOverviewMode(true);
    webView.getSettings().setUseWideViewPort(true);
    webView.setWebViewClient(new WebViewHandler(progressBar));

    webView.loadUrl(AUTHORISATION_REQUEST);
  }

  private class WebViewHandler extends WebViewClient {
    @NonNull
    private ProgressBar progressBar;

    WebViewHandler(@NonNull ProgressBar progressBar) {
      super();
      this.progressBar = progressBar;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (url.startsWith(CALLBACK_URL)) {
        UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(url.replace('#', '?'));
        String token = sanitizer.getValue("access_token");
        Credentials credentials = new Credentials(CLIENT_ID, token);

        Utils.showDirectoryContent(view.getContext(), credentials,
            DirectoryContentActivity.ROOT_DIRECTORY, false);
      } else {
        view.loadUrl(url);
      }
      return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      progressBar.setVisibility(View.GONE);
    }
  }
}
