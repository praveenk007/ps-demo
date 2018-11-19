package com.example.demo.utility;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientOptions;

import java.net.URI;

/**
 * @author praveenkamath
 **/
@Service
public class ReactiveWebClient {

    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector((HttpClientOptions.Builder builder) -> builder.disablePool()))
            .build();



    public <T> Mono<JsonNode> postClient(final String url, T postData) {
        System.out.println("postclient url :: "+url);
        return Mono.subscriberContext().flatMap(ctx -> {
            String cookieString = ctx.getOrDefault("cookies", StringUtils.EMPTY);
            return webClient.post().uri(URI.create(url)).body(BodyInserters.fromObject(postData)).header(HttpHeaders.COOKIE, cookieString)
                    .exchange().flatMap(clientResponse ->
                    {
                        return clientResponse.bodyToMono(JsonNode.class);
                    })
                    .doOnSuccess(jsonData -> {
                    });
        });
    }
}
