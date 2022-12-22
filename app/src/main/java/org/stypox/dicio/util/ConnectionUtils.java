package org.stypox.dicio.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

public final class ConnectionUtils {

    private ConnectionUtils() {
    }

    public static String getPage(final String url,
                                 final Map<String, String> headers) throws IOException {
        final URLConnection connection = new URL(url).openConnection();
        for (final Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        final Scanner scanner = new Scanner(connection.getInputStream());
        return scanner.useDelimiter("\\A").next();
    }

    public static String getPage(final String url) throws IOException {
        return getPage(url, Collections.emptyMap());
    }

    public static JSONObject getPageJson(final String url) throws IOException, JSONException {
        return new JSONObject(getPage(url));
    }

    public static String urlEncode(final String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "utf8");
    }

    public static String urlDecode(final String s) throws UnsupportedEncodingException {
        return URLDecoder.decode(s, "utf8");
    }
}
