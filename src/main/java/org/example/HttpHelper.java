package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpHelper {

    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT = 10_000;

    public HttpResult sendRequest(String method, String urlStr, String body, Map<String, String> headers) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        conn.setRequestProperty("Accept", "application/json");
        if (headers != null) {
            headers.forEach(conn::setRequestProperty);
        }

        if (body != null && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("PATCH"))) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (var os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        int status = conn.getResponseCode();
        InputStream is = status >= 200 && status < 400 ? conn.getInputStream() : conn.getErrorStream();

        String resp;
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                resp = br.lines().collect(Collectors.joining("\n"));
            }
        } else {
            resp = "";
        }

        conn.disconnect();
        return new HttpResult(status, resp);
    }

    public static class HttpResult {
        public final int status;
        public final String body;

        public HttpResult(int status, String body) {
            this.status = status;
            this.body = body;
        }

        public boolean is2xx() {
            return status >= 200 && status < 300;
        }
    }
}
