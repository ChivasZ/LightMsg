package com.lightmsg.plugin.airquality;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lightmsg.R;

public class AirQuality extends Activity {

    private WebView webview;
    //private String url = "http://zfb.ipe.org.cn/index.html?city_id=131&city_name=广州市&monitor_name=空气质量指数&lng=113.613889&lat=23.122361";
    private String url = "http://zfb.ipe.org.cn/index.html?city_id=100&city_name=广州市&monitor_name=空气质量指数";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity_layout);
        webview = (WebView) findViewById(R.id.webview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLoading();
    }

    private void startLoading() {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.loadUrl(url);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                if (newProgress == 100) {
                } else {
                }
            }
        });
    }
}
