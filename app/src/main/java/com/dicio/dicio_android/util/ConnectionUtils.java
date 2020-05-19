package com.dicio.dicio_android.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class ConnectionUtils {

    public static JSONObject getPageJson(String url) throws IOException, JSONException {
        URLConnection connection = new URL(url).openConnection();
        Scanner scanner = new Scanner(connection.getInputStream());
        String responseBody = scanner.useDelimiter("\\A").next();
        return new JSONObject(responseBody);
    }
}
