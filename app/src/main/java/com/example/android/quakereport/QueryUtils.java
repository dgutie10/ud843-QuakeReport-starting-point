package com.example.android.quakereport;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import static com.example.android.quakereport.EarthquakeActivity.LOG_TAG;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link Earthquake} objects that has been built up from
     * parsing a JSON response.
     */
    public static ArrayList<Earthquake> extractEarthquakes(String earthquakeJSON) {

        if (TextUtils.isEmpty(earthquakeJSON)) return null;

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Earthquake> earthquakes = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // TODO: Parse the response given by the SAMPLE_JSON_RESPONSE string and
            // build up a list of Earthquake objects with the corresponding data.

            JSONObject root = new JSONObject( earthquakeJSON);
            JSONArray earthquakeArray = root.getJSONArray("features");
            for (int i = 0; i < earthquakeArray.length(); i++){
                JSONObject currentEarthquake = earthquakeArray.getJSONObject(i);
                JSONObject properties = currentEarthquake.getJSONObject("properties");
                double magnitude = properties.getDouble("mag");
                String location = properties.getString("place");
                String time = properties.getString("time");
                String url = properties.getString("url");
                earthquakes.add(new Earthquake(magnitude,location,time, url));
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }

    public static List<Earthquake> fetchEarthquakeData(String requestURL){

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i(LOG_TAG,"fetchEarthquakeData initiated");
        URL url = createUrl(requestURL);
        String jsonResponse = null;

        try {jsonResponse = makeHttpRequest(url);}
        catch (IOException e){
            Log.e(LOG_TAG,"Problem making the HTTP request");
        }

        List<Earthquake> earthquakes = extractEarthquakes(jsonResponse);
        return earthquakes;
    }


    private static URL createUrl(String stringUrl){
        URL url = null;
        try{ url = new URL(stringUrl);}
        catch (MalformedURLException e ){
            Log.e(LOG_TAG, "Problem building url ",e);
        }
        return  url;
    }

    private static String makeHttpRequest (URL url) throws IOException{
        String jsonResponse = "";
        if (url == null) return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }else{
                Log.e(LOG_TAG,"Error response code: "+urlConnection.getResponseCode());
            }
        }catch (IOException e){
            Log.e(LOG_TAG,"Error retrieving data",e);
        }finally {
            if(urlConnection != null) urlConnection.disconnect();
            if(inputStream != null) inputStream.close();
        }
        return  jsonResponse;
    }

    private static String readFromStream (InputStream inputStream) throws IOException{
        StringBuilder output = new StringBuilder();
        if (inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null ){
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}