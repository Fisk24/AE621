package com.example.fisk.ae621;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

/**
 * Created by fisk on 2/27/18.

 Performs transactions with online api.

 Always manipulate from an AsyncTask or on a separate thread.

 */

public class ApiDelegate extends Fragment{

    private ApiCallback apiCallback; //Variable containing a reference to the activity that initialized the ApiDelegate

    public static final String TAG = "ApiDelegate";

    public static final int POLL_REQUEST = 0;
    public static final int POST_INDEX_REQUEST = 1;
    public static final int POST_SHOW_REQUEST = 2;
    public static final int POOL_INDEX_REQUEST = 3;
    public static final int POOL_SHOW_REQUEST = 4;
    public static final int COMMENT_INDEX_REQUEST = 5;
    public static final int COMMENT_SHOW_REQUEST = 6;

    private String baseUrl      = "https://e621.net";
    private String postIndex    = "/post/index.json";
    private String postShow     = "/post/show.json";
    private String commentIndex = "/comment/index.json";
    private String commentShow  = "/comment/show.json";

    private int postsPerPage = 75;

    private boolean doOmitFlash = true;

    public static ApiDelegate getInstance(FragmentManager fragmentManager) {
        ApiDelegate apiDelegate = new ApiDelegate();
        fragmentManager.beginTransaction().add(apiDelegate, TAG).commit();
        return apiDelegate;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // By creating the reference when the fragment is attached, it will be automatically revised when the activity is recreated
        // This prevents async task from accidentally evoking a destroyed instance of the parent activity
        apiCallback = (ApiCallback) context;
        Log.e("DEBUG", "onAttach was called.");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        /**
         * Set the callback to null so we don't accidentally leak the
         * Activity instance.
         *
         * I don't know what this means, but it sounds bad so lets not let that happen.
         */
        apiCallback = null;
    }

    public void omitFlash(boolean choice) {
        doOmitFlash = choice;
    }

    private String getRawJsonResponse(String urlString) throws java.net.UnknownHostException, javax.net.ssl.SSLHandshakeException{
        try {
            // Download
            // Todo: Set the user-agent as per e621's guidelines
            URL url = new URL(urlString);

            InputStream inputStream = url.openConnection().getInputStream();

            // Read InputStream
            InputStreamReader streamReader = new InputStreamReader(inputStream);

            // Raw Json String
            String rawString = readAll(streamReader);

            // Close streams
            streamReader.close();
            inputStream.close();

            // Return raw json string
            return rawString;

        }
        catch (java.net.UnknownHostException e) {
            Log.e("API_DELEGATE", "getRawJsonResponse: UnknownHost"+e.toString());
            throw e;
        }
        catch (javax.net.ssl.SSLHandshakeException e) {
            Log.e("API_DELEGATE", "getRawJsonResponse: SSLHandshake"+e.toString());
            throw e;
        }
        catch (IOException e) {
            Log.e("API_DELEGATE", "getRawJsonResponse: IOException: "+e.toString());
        }
        // TODO: Using HttpsUrlConnection supplies getResponseCode() for checking the status code
        // Consider using returning the status code instead of an empty string
        // Return the status code in Json format in the other functions
        return "";
    }

    private String readAll(InputStreamReader streamReader) throws IOException {
        // Build the string response

        String inputLine;

        BufferedReader reader        = new BufferedReader(streamReader);
        StringBuilder  stringBuilder = new StringBuilder();

        while((inputLine = reader.readLine()) != null) {
            stringBuilder.append(inputLine);
        }

        reader.close();
        return stringBuilder.toString();
    }

    private JSONArray removeFlashPosts(JSONArray data) {
        String criteriaKey   = "file_ext";
        String criteriaValue = "swf";

        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                if (jsonObject.getString(criteriaKey).equals(criteriaValue)) {
                    Log.i("ApiDelegate", "Removed flash oriented post id:"+jsonObject.getString("id")+" at index:"+i);
                    data.remove(i);
                }
            }
        } catch (JSONException e) {
            Log.e("ApiDelegate", "removeFlashPosts(): " + e.toString());
        }

        return data;
    }

    private boolean performPollingQuery() {
        URL url = null;
        try {
            url = new URL("https://e621.net/");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();

            Log.e("FUNCTIONTEST", "PollingQuery: "+code);
            if (code == 200) {
                return true;
            }
        } catch (MalformedURLException e) {
            Log.e("ApiDelegate", "PollingQuery: "+e.toString());
        } catch (IOException e) {
            Log.e("ApiDelegate", "PollingQuery: "+e.toString());
        }

        return false;
    }

    public JSONArray performBasicPostIndexQuery(int page) throws JSONException, SSLHandshakeException, UnknownHostException {
        String finalUrl = baseUrl+postIndex+"?limit="+postsPerPage+"&page="+page;
        JSONArray parsedData = new JSONArray(getRawJsonResponse(finalUrl));
        if (doOmitFlash) {
            return removeFlashPosts(parsedData);
        }
        return parsedData;
    }

    public JSONArray performTaggedPostIndexSearchQuery(String tags, int page) throws JSONException, SSLHandshakeException, UnknownHostException {
        String finalUrl;
        String refinedTags;

        refinedTags = tags.replace(" ", "%20");
        finalUrl = baseUrl+postIndex+"?limit="+postsPerPage+"&tags="+refinedTags;

        JSONArray parsedData = new JSONArray(getRawJsonResponse(finalUrl));
        if (doOmitFlash) {
            return removeFlashPosts(parsedData);
        }
        return parsedData;
    }

    // AsyncTask networking boiler plate

    public interface ApiCallback {
        void onApiResponse(ApiResponse apiResponse);

        NetworkInfo getActiveNetworkInfo();

        /* This could be useful in some use cases but not all. Can an interface be overloaded?
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute();
         */
    }

    public void performQueryById(int id) {
        if (this.apiCallback != null) {
            new ApiTask(id).execute();
        }
    }

    public void setApiCallback(ApiCallback apiCallback) {
        this.apiCallback = apiCallback;
    }

    // Generic response object designed to contain any and all information the api may wish to return
    // Do not manually create instances of this class outside of this file
    public class ApiResponse {
        private boolean requestSuccessful;
        private JSONArray responseData;

        public ApiResponse(boolean success) {
            this.requestSuccessful = success;
        }

        public ApiResponse(boolean success, JSONArray responseData) {
            this.requestSuccessful = success;
            this.responseData = responseData;
        }

        public boolean wasSuccess() {
            return requestSuccessful;
        }

        public JSONArray getResponseData() {
            return responseData;
        }
    }

    private class ApiTask extends AsyncTask <Void, Void, Boolean>{

        private int intendedTask;

        ApiTask(int id) {
            intendedTask = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (apiCallback != null) {
                NetworkInfo netInfo = apiCallback.getActiveNetworkInfo();
                //Log.e("DEBUG", "ApiCallback is not null");
                // Invert the affirmative, If this condition is TRUE then the code block will NOT run
                if (!(netInfo != null && netInfo.isConnected())) {
                    //Log.e("DEBUG", "Internet is not connected");
                    //TODO: Must add a reason for failure
                    // If the internet is not connected, cancel the operation and evoke the call back to report a failed query
                    cancel(true);
                    apiCallback.onApiResponse(new ApiResponse(false));
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //TODO: Maybe doInBackground could return an ApiResponse Object
            // ID based task switcher
            switch (intendedTask) {
                // Each point of functionality that the delegate possesses must have a case in this block
                case POLL_REQUEST:
                    return performPollingQuery();

            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            apiCallback.onApiResponse(new ApiResponse(success));
        }
    }

}
