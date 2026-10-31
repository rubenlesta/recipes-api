package com.aldaba.recipes.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.aldaba.recipes.client.dto.SpoonacularSearchResponse;
import com.aldaba.recipes.exception.ExternalApiException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

class SpoonacularClientTest {

    private static final String API_KEY = "test-api-key";
    private static final String BASE_URL = "https://api.spoonacular.test";

    private MockRestServiceServer server;
    private SpoonacularClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("x-api-key", API_KEY);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SpoonacularClient(builder.build(), API_KEY);
    }

    @Test
    void returnsFirstRecipeWithDetails() throws Exception {
        String fixture = new ClassPathResource("spoonacular-pasta.json")
                .getContentAsString(StandardCharsets.UTF_8);
        expectSearchRequest(
                "Ratatouille Pasta & Herbs",
                withSuccess(fixture, MediaType.APPLICATION_JSON));

        SpoonacularSearchResponse.Result recipe =
                client.findFirstByName("Ratatouille Pasta & Herbs").orElseThrow();

        assertEquals("Ratatouille Pasta", recipe.title());
        assertEquals(45, recipe.readyInMinutes());
        assertEquals(4, recipe.servings());
        assertEquals(12, recipe.extendedIngredients().size());
        assertEquals(6, recipe.analyzedInstructions().stream()
                .flatMap(group -> group.steps().stream())
                .count());
        assertEquals("https://img.spoonacular.com/recipes/657933-312x231.jpg", recipe.image());
        server.verify();
    }

    @Test
    void returnsEmptyWhenThereAreNoResults() {
        expectSearchRequest(
                "missing recipe",
                withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));

        assertTrue(client.findFirstByName("missing recipe").isEmpty());
        server.verify();
    }

    @Test
    void encodesSpecialCharactersInName() {
        String name = "mac & cheese {x} +1";
        expectSearchRequest(
                name,
                withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));

        assertTrue(client.findFirstByName(name).isEmpty());
        server.verify();
    }

    @Test
    void failsWithoutCallingApiWhenKeyIsBlank() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        MockRestServiceServer noCalls = MockRestServiceServer.bindTo(builder).build();
        SpoonacularClient noKeyClient = new SpoonacularClient(builder.build(), " ");

        assertThrows(
                ExternalApiException.class,
                () -> noKeyClient.findFirstByName("pasta"));
        noCalls.verify();
    }

    @Test
    void wrapsUnauthorizedResponse() {
        expectSearchRequest("Ratatouille Pasta", withStatus(HttpStatus.UNAUTHORIZED));

        ExternalApiException exception = assertThrows(
                ExternalApiException.class,
                () -> client.findFirstByName("Ratatouille Pasta"));

        assertTrue(exception.getMessage().contains("401"));
        assertFalse(exception.getMessage().contains(API_KEY));
        server.verify();
    }

    @Test
    void wrapsServerErrorResponse() {
        expectSearchRequest("Ratatouille Pasta", withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ExternalApiException exception = assertThrows(
                ExternalApiException.class,
                () -> client.findFirstByName("Ratatouille Pasta"));

        assertTrue(exception.getMessage().contains("500"));
        assertFalse(exception.getMessage().contains(API_KEY));
        server.verify();
    }

    private void expectSearchRequest(String name, ResponseCreator response) {
        server.expect((RequestMatcher) request -> {
                    var uri = request.getURI();
                    assertEquals(BASE_URL, uri.getScheme() + "://" + uri.getAuthority());
                    assertEquals("/recipes/complexSearch", uri.getPath());
                    var query = UriComponentsBuilder
                            .fromUri(uri)
                            .build()
                            .getQueryParams();
                    assertEquals(
                            name,
                            URLDecoder.decode(query.getFirst("query"), StandardCharsets.UTF_8));
                    assertEquals("1", query.getFirst("number"));
                    assertEquals("true", query.getFirst("fillIngredients"));
                    assertEquals("true", query.getFirst("addRecipeInstructions"));
                    assertEquals("true", query.getFirst("addRecipeInformation"));
                })
                .andExpect(method(HttpMethod.GET))
                .andExpect(request -> {
                    assertEquals(API_KEY, request.getHeaders().getFirst("x-api-key"));
                    assertFalse(request.getURI().toString().contains(API_KEY));
                })
                .andRespond(response);
    }
}
