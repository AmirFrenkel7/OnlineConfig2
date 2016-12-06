package frenkel.amir.onlineconfiglib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by mypc on 6/12/2016.
 */

public class ConfigManager {

    private Context context;

    private Map<String, String> map = new HashMap<String, String>();
    private Map<String, Boolean> usedInMap = new HashMap<String, Boolean>();
    private boolean isUseFirstUrl = true;

    private final String SHARED_PREFS_NAME = "ONLINE_CONFIG_LIB";
    private final String DID_USER_LEAVE = "DID_USER_LEAVE";
    private final String JSON_URL1 = "http://md5.jsontest.com/?text=one_text";
    private final String JSON_URL2 = "http://md5.jsontest.com/?text=another_text";

    public Map<String, String> getMap() {
        return map;
    }
    public Map<String, Boolean> getUsedInMap() {
        return usedInMap;
    }
    public void setIsUseFirstUrl(boolean value) {
        isUseFirstUrl = value;
    }

    //Getting Configuration & Storing In Shared Preferences
    public void getConfiguration(final Context context, final boolean isWaitSynchronously) {
        //here i need to wait for this function, if map is empty
        //map = new HashMap<String, String>();
        CountDownLatch startSignal = new CountDownLatch(0);
        if (map.isEmpty() || isWaitSynchronously) startSignal = new CountDownLatch(1);
        getConfigurationFromWebAndSaveInSharedPrefs(context, startSignal);//, null);
    }
    public void getConfigurationFromWebAndSaveInSharedPrefs(final Context context, final CountDownLatch startSignal) {//, final ServerCallback callback) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, isUseFirstUrl ? JSON_URL1 : JSON_URL2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        handleResponse(context, startSignal, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorResponse(context, startSignal);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }
    public void handleResponse(Context context, CountDownLatch startSignal, String response) {
        //String json = "{\"md5\":\"v1\",\"original\":\"v2\"}";
        Map<String, String> innerMap = new HashMap<String, String>();
        Gson gson = new Gson();
        innerMap = (Map<String, String>) gson.fromJson(response, innerMap.getClass());

        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
        for (Map.Entry<String, String> entry : innerMap.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.commit();
        getCachedMap(context);
        startSignal.countDown();

    }
    public void errorResponse(Context context, CountDownLatch startSignal) {
        //SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
        //editor.clear();
        //editor.commit();
        map = new HashMap<>();
        startSignal.countDown();
    }

    //Cache Checks - Using Shared Preferences
    public boolean isExistsInCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, 0);

        if (prefs == null) return false;
        if (prefs.getAll().size() == 0) return false;
        return true;
    }
    public Map<String, String> getCachedMap(Context context) {

        if (!isExistsInCache(context)) return null;

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, 0);

        if (map.isEmpty()) {
            //Load Map from Shared Preferences
            map = new HashMap<String, String>();
            for (Map.Entry entry : prefs.getAll().entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue().toString());
                usedInMap.put(entry.getKey().toString(), false);
            }
        }
        else {
            for (Map.Entry entry : prefs.getAll().entrySet()) {
                String key = entry.getKey().toString();
                if (usedInMap.containsKey(key) && usedInMap.get(key) == false)
                    map.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return map;
    }
    public void setUsedItem(String key) {
        usedInMap.put(key, true);
    }
    public void toggleUsedItem(String key) {
        if (!usedInMap.containsKey(key)) {
            setUsedItem(key);
            return;
        }

        usedInMap.put(key, !usedInMap.get(key));
    }
    public void clearCache(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
        editor.clear();
        editor.commit();
    }

    //Crash Persistence - Using Shared Preferences
    public boolean loadDidUserLeaveSavedPreference(final Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean didUserLeave = sharedPreferences.getBoolean(DID_USER_LEAVE, true);
        return didUserLeave;
    }
    public void saveDidUserLeavePreference(final Context context, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DID_USER_LEAVE, value);
        editor.commit();
    }
}
