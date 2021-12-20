package org.meeboo.component;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpClientComponent {

    public String httpClientGet(String uri) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(60))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json; character=utf-8")
                .header("Accept", "application/json")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
