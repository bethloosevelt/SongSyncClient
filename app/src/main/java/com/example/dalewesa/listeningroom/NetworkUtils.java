package com.example.dalewesa.listeningroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;


//
public class NetworkUtils {
    /**
     * Gets an input stream from a url
     * @param dataUrl the url to get the input stream from
     * @return an input stream of the data contained at the url
     */
    private static InputStream getInputStream(String dataUrl) throws Exception {
        // Create an HTTP connection to the endpoint
        URL url = new URL(dataUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        conn.connect();
        int response = conn.getResponseCode();

        return conn.getInputStream();
    }

    /**
     * Gets JSON data from a static URL endpoint
     * @return the string containing all the json data
     */
    static public String getJson(String jsonURL) throws IOException {
        InputStream is = null;
        String json = "";

        try {
            is = NetworkUtils.getInputStream(jsonURL);

            // Convert stream to string
            json = readIt(is);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return json;
    }

    /**
     * Reads an InputStream and converts it to a String.
     */
    static private String readIt(InputStream stream) throws IOException {
        Reader isReader = new InputStreamReader(stream, "UTF-8");
        BufferedReader reader = new BufferedReader(isReader);
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            line += "\n";
            out.append(line);
        }
        reader.close();

        return out.toString();
    }
}
