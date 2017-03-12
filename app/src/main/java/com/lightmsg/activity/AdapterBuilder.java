package com.lightmsg.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import com.lightmsg.http.HttpLoader;

import android.util.Log;

public abstract class AdapterBuilder {
    protected static final String TAG = "AdapterBuilder";
    protected abstract ArrayList<HashMap<String, Object>> parse(String json) throws JSONException;

    protected static final HttpLoader httploader = new HttpLoader();
    /**
     * Create a suitable Adapter for ListView.
     * 
     * @param url
     * @throws JSONException
     */
    protected void create(ViewAdapter va, String url) throws JSONException {
        String json = httploader.doGet(url);
        if (json != null && !json.isEmpty()) {
            va.setDataList(parse(json));
            va.initDataList(true);
        } else {
            va.initDataList(false);
        }
    }

}
