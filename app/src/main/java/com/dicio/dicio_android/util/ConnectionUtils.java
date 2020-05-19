package com.dicio.dicio_android.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

public class ConnectionUtils {

    public static JSONObject getPageJson(String url) throws IOException, JSONException {
        URLConnection connection = new URL(url).openConnection();
        Scanner scanner = new Scanner(connection.getInputStream());
        String responseBody = scanner.useDelimiter("\\A").next();
        return new JSONObject(responseBody);
    }

    public static String urlencode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "utf8");
    }
}
