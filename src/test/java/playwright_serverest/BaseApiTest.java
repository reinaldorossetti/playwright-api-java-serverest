package playwright_serverest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;

/**
 * Base class for all Playwright API tests.
 * Manages Playwright and APIRequestContext lifecycle per test class.
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class BaseApiTest {

    protected static final String BASE_URL = "https://serverest.dev";

    protected Playwright playwright;
    protected APIRequestContext request;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    void setupPlaywright() {
        playwright = Playwright.create();
        request = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(BASE_URL));
    }

    @AfterAll
    void teardownPlaywright() {
        if (request != null) {
            request.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    /**
     * Builds a JSON string from key-value pairs.
     */
    protected String buildJson(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("keyValues must be pairs");
        }
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    /**
     * Reads a JSON resource file from the classpath and returns its content as a Map.
     */
    protected Map<String, Object> loadJsonResource(String resourcePath) throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + resourcePath);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(is, Map.class);
            return map;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parseResponseBody(APIResponse response) throws Exception {
        return objectMapper.readValue(response.body(), Map.class);
    }
}
