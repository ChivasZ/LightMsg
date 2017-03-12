package com.lightmsg.plugin.weather;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lightmsg.R;

public class WeatherActivity extends Activity {

    private WebView webview;
    private String url = "http://wxc.gd121.cn/html/weixinportal/z_ali_wp4.html?districtcode=440100&isnavi=false";
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
