package com.lightmsg.http;

import java.io.IOException;
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

import android.util.Log;

public class HttpLoader {

    private static final String TAG = HttpLoader.class.getSimpleName();
    
    /**
     * Access the server to fetch JSON data by POST.
     * 
     * @param params to the server
     * @param url
     * @return
     * @throws Exception
     */
    public static String doPost(List<NameValuePair> params, String url)
            throws Exception {
        String result = null;
        HttpClient httpClient = new DefaultHttpClient();

        /*
         * Example of NameValuePair.
         * 
        List<NameValuePair> params  =new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", "hello"));
        params.add(new BasicNameValuePair("password", "eoe"));
        */


        HttpPost httpPost = new HttpPost(url);
        if (params != null) {
            HttpEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            httpPost.setEntity(entity);
        }

        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000);

        try {
            HttpResponse httpResp = httpClient.execute(httpPost);
            if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(httpResp.getEntity(), "UTF-8");
            } else {
                // Not correct responded.
                Log.e(TAG, "HttpPost:" + "HttpPost request failed!!");
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Access the server to fetch JSON data by GET.
     * 
     * @param url
     * @return
     */
    public static String doGet(String url) {
        String result = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), 3000);
        ConnManagerParams.setTimeout(httpClient.getParams(), 10000);

        HttpGet httpget = null;
        try {
            httpget = new HttpGet(url);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse response = httpClient.execute(httpget);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity(),	"UTF-8");
            } else {
                // Not correct responded.
                Log.e(TAG, "HttpGet:" + "HttpGet request failed!!");
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return result;
    }
}
