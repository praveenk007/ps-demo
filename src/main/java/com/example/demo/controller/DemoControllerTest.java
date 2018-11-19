package com.example.demo.controller;

import com.example.demo.utility.ReactiveWebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author praveenkamath
 **/
@CrossOrigin(origins = "*")
@RestController
public class DemoControllerTest {

    @Autowired
    ReactiveWebClient reactiveWebClient;

    private static final ThreadLocal<String> dbName = new ThreadLocal<String>();

    public static void setDatabaseNameForCurrentThread(final String databaseName) {
        dbName.set(databaseName);
    }

    @RequestMapping(value = "/api/testrequest", method = RequestMethod.POST)
    @ResponseBody
    public Mono<String> getRequest() {
        doAThing();
        return Mono.just("ok");
    }

    private void doAThing() {
        JsonNode postData = new ObjectMapper().createObjectNode();
        ((ObjectNode) postData).put("test", "value");
        for (int i = 1; i < 40; i++) {
            asyncSomething(postData, i);
        }
    }

    private void asyncSomething(JsonNode postData, int i) {
        String url = "http://localhost:9050/api/test/plan"+i;
        reactiveWebClient.postClient(url, postData)
                .subscribe(resp -> {
                    System.out.println("Response "+resp.get("key"));
                    doSomething().subscriberContext(context -> {
                        setDatabaseNameForCurrentThread("dbTest");
                        return context;
                    }).subscribe();
                });
    }

    Mono<String> doSomething() {
        return Mono.create(
                sink -> {
                    try {
                        Thread.sleep((int)(Math.random()*10*100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sink.success("ok");
                });
    }

    @RequestMapping(value = "/api/test/{key}", method = RequestMethod.POST)
    @ResponseBody
    public JsonNode test(@RequestBody JsonNode request, @PathVariable String key) {
        int sleep = (int)(Math.random()*10*500);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JsonNode jsonNode = new ObjectMapper().createObjectNode();
        ((ObjectNode) jsonNode).put("key", key);
        return jsonNode;
    }

}
