package com.lightmsg.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class GridViewAdapterBuilder extends AdapterBuilder {

    ArrayList<HashMap<String, Object>> list;
    
    /**
     * Parse JSON String.
     * 
     * @throws JSONException
     */
    protected ArrayList<HashMap<String, Object>> parse(String jsonStr)
            throws JSONException {
        JSONArray jsonArray = null;
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", jsonObject.getString("id"));
            map.put("name", jsonObject.getString("name"));
            map.put("icon", jsonObject.getString("icon"));
            map.put("description", jsonObject.getString("description"));
            map.put("action", jsonObject.getString("action"));
            list.add(map);
        }
        return list;
    }
}
