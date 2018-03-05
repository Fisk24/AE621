package com.example.fisk.ae621;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by fisk on 2/27/18.

 Performs transactions with online api.

 Always manipulate from an AsyncTask or on a separate thread.

 */

public class ApiDelegate {

    private String baseUrl = "https://e621.net";
    private String postIndex = "/post/index.json";
    private String commentIndex = "/comment/index.json";
    private String postShow = "/post/show.json";
    private String commentShow = "/comment/show.json";

    private int postsPerPage = 75;

    private boolean doOmitFlash = true;

    public void omitFlash(boolean choice) {
        doOmitFlash = choice;
    }

    private String getRawJsonResponse(String urlString) {
        try {
            // Download
            // Todo: Set the user-agent as per e621's guidelines
            // Todo: ApiDeligate might be more useful with its own thread, instead of having AsyncTasks Everywhere
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
        } catch (IOException e) {
            Log.e("FetchFeedTask", e.toString());
        }

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

    public JSONArray performBasicPostIndexQuery(int page) throws JSONException {
        String finalUrl = baseUrl+postIndex+"?limit="+postsPerPage+"&page="+page;
        JSONArray parsedData = new JSONArray(getRawJsonResponse(finalUrl));
        if (doOmitFlash) {
            return removeFlashPosts(parsedData);
        }
        return parsedData;
    }

    public JSONArray performTaggedPostIndexSearchQuery(String tags, int page) throws JSONException {
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

}
