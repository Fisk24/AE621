package com.example.fisk.ae621;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

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

    String baseUrl = "https://e621.net";
    String postIndex = "/post/index.json";
    String commentIndex = "/comment/index.json";
    String postShow = "/post/show.json";
    String commentShow = "/comment/show.json";

    int postsPerPage = 75;

    private String getRawJsonResponse(String urlString) {
        try {
            // Download
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

    public JSONArray performBasicPostIndexQuery(int page) throws JSONException {
        String finalUrl = baseUrl+postIndex+"?limit="+postsPerPage+"&page="+page;
    return new JSONArray(getRawJsonResponse(finalUrl));
    }

    public JSONArray performTaggedPostIndexSearchQuery(String tags, int page) throws JSONException {
        String finalUrl;
        String refinedTags;

        refinedTags = tags.replace(" ", "%20");
        finalUrl = baseUrl+postIndex+"?limit="+postsPerPage+"&tags="+refinedTags;

        return new JSONArray(getRawJsonResponse(finalUrl));
    }

}
