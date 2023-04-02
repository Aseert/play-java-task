package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.Person;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class StartWarsClient {

    private final WSClient ws;
    private final String apiUrl;

    @Inject
    public StartWarsClient(WSClient ws, Config config) {
        this.ws = ws;
        this.apiUrl = config.getString("api-url");
    }

    /**
     * get planet example
     *
     * @return JSON Node that can be converted to Planet class
     */
    public CompletionStage<JsonNode> getFirstPlanet() {
        return ws.url(apiUrl + "planets/1")
                 .get()
                 .thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> getPlanet(String id) {
        return ws.url(apiUrl + "planets/" + id)
                 .get()
                 .thenApply(WSResponse::asJson);
    }

    public List<Person> getPeople(List<String> urlPeople) {
        List<CompletableFuture<Person>> futures = new ArrayList<>();
        for (String url : urlPeople) {
            CompletableFuture<Person> future = getPerson(url);
            futures.add(future);
        }

        List<Person> people = new ArrayList<>();
        for (CompletableFuture<Person> future : futures) {
            people.add(future.join());
        }
        return people;
    }

    private CompletableFuture<Person> getPerson(String url) {
        CompletionStage<WSResponse> responsePromise = ws.url(url)
                                                        .get();

        return responsePromise.thenApply(response -> {
                                  JsonNode json = response.asJson();
                                  return Json.fromJson(json, Person.class);
                              })
                              .toCompletableFuture();
    }
}
