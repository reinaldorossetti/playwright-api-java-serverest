package playwright_serverest.produtos;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import playwright_serverest.BaseApiTest;

@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
@SuppressWarnings("unchecked")
public class ProductsPlaywrightTest extends BaseApiTest {

    private String getAdminToken() throws Exception {
        String email = "admin." + UUID.randomUUID() + "@example.com";
        String password = "SenhaSegura@123";

        String userPayload = String.format(
                "{\n" +
                        "  \"nome\": \"Admin User\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"%s\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}",
                email, password);

        request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(userPayload));

        String loginBody = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
        APIResponse loginResp = request.post("/login", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(loginBody));
        assertEquals(200, loginResp.status());

        Map<String, Object> loginBody2 = objectMapper.readValue(loginResp.body(), Map.class);
        return (String) loginBody2.get("authorization");
    }

    @Test
    @DisplayName("CT01 - List all products and validate JSON structure")
    void ct01_listProducts() throws Exception {
        APIResponse resp = request.get("/produtos");
        assertEquals(200, resp.status());

        Map<String, Object> body = parseResponseBody(resp);
        int quantidade = (int) body.get("quantidade");
        List<Map<String, Object>> produtos = (List<Map<String, Object>>) body.get("produtos");

        assertTrue(quantidade >= 0);
        assertNotNull(produtos);

        for (Map<String, Object> product : produtos) {
            assertTrue(product.containsKey("nome"));
            assertTrue(product.containsKey("preco"));
            assertTrue(product.containsKey("descricao"));
            assertTrue(product.containsKey("quantidade"));
            assertTrue(product.containsKey("_id"));
        }
    }

    @Test
    @DisplayName("CT02 - Create a new product as an administrator")
    void ct02_createProductAsAdmin() throws Exception {
        String token = getAdminToken();
        String productName = "Product " + System.currentTimeMillis();
        String productPayload = String.format(
                "{\n" +
                        "  \"nome\": \"%s\",\n" +
                        "  \"preco\": 250,\n" +
                        "  \"descricao\": \"Automated test product\",\n" +
                        "  \"quantidade\": 100\n" +
                        "}",
                productName);

        APIResponse createResp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(productPayload));

        assertEquals(201, createResp.status());

        Map<String, Object> createBody = parseResponseBody(createResp);
        assertEquals("Cadastro realizado com sucesso", createBody.get("message"));
        assertNotNull(createBody.get("_id"));

        String productId = (String) createBody.get("_id");

        APIResponse getResp = request.get("/produtos/" + productId);
        assertEquals(200, getResp.status());

        Map<String, Object> product = objectMapper.readValue(getResp.body(), Map.class);
        assertEquals(productName, product.get("nome"));
        assertEquals(250, product.get("preco"));
        assertEquals(100, product.get("quantidade"));
    }

    @Test
    @DisplayName("CT03 - Validate error when creating a product with a duplicate name")
    void ct03_duplicateProductName() throws Exception {
        String token = getAdminToken();
        String name = "Duplicate Product Test " + System.currentTimeMillis();

        String productPayload = String.format(
                "{\n" +
                        "  \"nome\": \"%s\",\n" +
                        "  \"preco\": 150,\n" +
                        "  \"descricao\": \"First product\",\n" +
                        "  \"quantidade\": 50\n" +
                        "}",
                name);

        APIResponse first = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(productPayload));
        assertEquals(201, first.status());

        APIResponse second = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(productPayload));
        assertEquals(400, second.status());

        Map<String, Object> secondBody = objectMapper.readValue(second.body(), Map.class);
        assertEquals("Já existe produto com esse nome", secondBody.get("message"));
    }

    @Test
    @DisplayName("CT04 - Search for products using query parameters")
    void ct04_searchForProductsWithFilters() throws Exception {
        APIResponse resp = request.get("/produtos?nome=Logitech");
        assertEquals(200, resp.status());

        Map<String, Object> body = parseResponseBody(resp);
        List<Map<String, Object>> produtos = (List<Map<String, Object>>) body.get("produtos");
        if (produtos != null && !produtos.isEmpty()) {
            for (Map<String, Object> p : produtos) {
                assertTrue(((String) p.get("nome")).contains("Logitech"),
                        "Product name should contain 'Logitech'");
            }
        }

        APIResponse priceResp = request.get("/produtos?preco=100");
        assertEquals(200, priceResp.status());
    }

    @Test
    @DisplayName("CT05 - Update information of an existing product")
    void ct05_updateExistingProduct() throws Exception {
        String token = getAdminToken();
        String productName = "Product " + System.currentTimeMillis();

        String initialProduct = String.format(
                "{\n" +
                        "  \"nome\": \"%s\",\n" +
                        "  \"preco\": 100,\n" +
                        "  \"descricao\": \"Original description\",\n" +
                        "  \"quantidade\": 50\n" +
                        "}",
                productName);

        APIResponse createResp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(initialProduct));
        assertEquals(201, createResp.status());

        Map<String, Object> createBody = objectMapper.readValue(createResp.body(), Map.class);
        String productId = (String) createBody.get("_id");

        String updatedProduct = String.format(
                "{\n" +
                        "  \"nome\": \"%s\",\n" +
                        "  \"preco\": 200,\n" +
                        "  \"descricao\": \"Updated description\",\n" +
                        "  \"quantidade\": 75\n" +
                        "}",
                productName);

        APIResponse updateResp = request.put("/produtos/" + productId, RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(updatedProduct));
        assertEquals(200, updateResp.status());

        Map<String, Object> updateBody = objectMapper.readValue(updateResp.body(), Map.class);
        assertEquals("Registro alterado com sucesso", updateBody.get("message"));

        APIResponse getResp = request.get("/produtos/" + productId);
        assertEquals(200, getResp.status());

        Map<String, Object> product = objectMapper.readValue(getResp.body(), Map.class);
        assertEquals(200, product.get("preco"));
        assertEquals("Updated description", product.get("descricao"));
        assertEquals(75, product.get("quantidade"));
    }

    @Test
    @DisplayName("CT06 - Validate price calculations and comparisons")
    void ct06_validatePriceCalculationsAndComparisons() throws Exception {
        APIResponse resp = request.get("/produtos");
        assertEquals(200, resp.status());

        Map<String, Object> body = parseResponseBody(resp);
        List<Map<String, Object>> produtos = (List<Map<String, Object>>) body.get("produtos");

        if (produtos == null || produtos.isEmpty()) {
            return;
        }

        List<Number> prices = produtos.stream()
                .map(p -> (Number) p.get("preco"))
                .collect(Collectors.toList());

        double maxPrice = prices.stream().mapToDouble(Number::doubleValue).max().orElse(0);
        double minPrice = prices.stream().mapToDouble(Number::doubleValue).min().orElse(0);

        for (Number price : prices) {
            assertTrue(price.doubleValue() > 0, "Price should be greater than 0");
            assertTrue(price.doubleValue() < 100000, "Price should be less than 100000");
        }

        assertTrue(maxPrice >= minPrice, "Max price should be >= min price");
    }

    @Test
    @DisplayName("CT07 - Attempt to create a product without an authentication token")
    void ct07_createProductWithoutToken() throws Exception {
        String productPayload = "{\n" +
                "  \"nome\": \"Product Without Auth\",\n" +
                "  \"preco\": 100,\n" +
                "  \"descricao\": \"Test\",\n" +
                "  \"quantidade\": 10\n" +
                "}";

        APIResponse resp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(productPayload));

        assertEquals(401, resp.status());
        Map<String, Object> body = parseResponseBody(resp);
        assertEquals("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais",
                body.get("message"));
    }

    @DisplayName("CT08 - Validate required fields when creating a product")
    @ParameterizedTest(name = "CT08 - Validate required fields when creating a product")
    @ValueSource(ints = {1, 2, 3, 4})
    void ct08_validateRequiredFieldsWhenCreatingProduct(int numberField) throws Exception {
        String token = getAdminToken();
                String payload;
                switch (numberField) {
                        case 1:
                                payload = "{\"preco\": 0.55, \"descricao\": \"Test without name\", \"quantidade\": 10}";
                                break;
                        case 2:
                                payload = "{\"nome\": \"Product Without Description\", \"descricao\": \"\", \"quantidade\": 10}";
                                break;
                        case 3:
                                payload = "{\"nome\": \"Product Without Quantity\", \"preco\": 100, \"quantidade\": -1}";
                                break;
                        case 4:
                                payload = "{\"nome\": \"null\", \"preco\": 1.99, \"descricao\": \"null\"}";
                                break;
                        default:
                                throw new IllegalArgumentException("Unknown field: " + numberField);
                }

        APIResponse resp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(payload));

        assertEquals(400, resp.status());
    }

    @Test
    @DisplayName("CT09 - Work with complex JSON data")
    void ct09_workWithComplexJsonData() throws Exception {
        APIResponse resp = request.get("/produtos");
        assertEquals(200, resp.status());

        Map<String, Object> body = parseResponseBody(resp);
        List<Map<String, Object>> produtos = (List<Map<String, Object>>) body.get("produtos");

        if (produtos == null) {
            return;
        }

        List<Map<String, Object>> cheapProducts = produtos.stream()
                .filter(p -> ((Number) p.get("preco")).doubleValue() < 100)
                .collect(Collectors.toList());
        List<Map<String, Object>> mediumProducts = produtos.stream()
                .filter(p -> {
                    double price = ((Number) p.get("preco")).doubleValue();
                    return price >= 100 && price < 500;
                })
                .collect(Collectors.toList());
        List<Map<String, Object>> expensiveProducts = produtos.stream()
                .filter(p -> ((Number) p.get("preco")).doubleValue() >= 500)
                .collect(Collectors.toList());

        assertNotNull(cheapProducts);
        assertNotNull(mediumProducts);
        assertNotNull(expensiveProducts);
    }

    @Test
    @DisplayName("CT10 - Delete an existing product")
    void ct10_deleteExistingProduct() throws Exception {
        String token = getAdminToken();
        String productName = "Product " + System.currentTimeMillis();

        String productPayload = String.format(
                "{\n" +
                        "  \"nome\": \"%s\",\n" +
                        "  \"preco\": 100,\n" +
                        "  \"descricao\": \"Product to delete\",\n" +
                        "  \"quantidade\": 10\n" +
                        "}",
                productName);

        APIResponse createResp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(productPayload));
        assertEquals(201, createResp.status());

        Map<String, Object> createBody = objectMapper.readValue(createResp.body(), Map.class);
        String productId = (String) createBody.get("_id");

        APIResponse deleteResp = request.delete("/produtos/" + productId, RequestOptions.create()
                .setHeader("Authorization", token));
        assertEquals(200, deleteResp.status());

        Map<String, Object> deleteBody = objectMapper.readValue(deleteResp.body(), Map.class);
        assertEquals("Registro excluído com sucesso", deleteBody.get("message"));

        APIResponse getResp = request.get("/produtos/" + productId);
        assertEquals(400, getResp.status());

        Map<String, Object> getBody = objectMapper.readValue(getResp.body(), Map.class);
        assertEquals("Produto não encontrado", getBody.get("message"));
    }

    @Test
    @DisplayName("CT11 - Create a product from fixed JSON payload")
    void ct11_createProductFromFixedJsonPayload() throws Exception {
        String token = getAdminToken();

        Map<String, Object> productPayload = loadJsonResource("playwright_serverest/produtos/resources/productPayload.json");
        productPayload.put("nome", "Product " + System.currentTimeMillis());

        APIResponse resp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(objectMapper.writeValueAsString(productPayload)));

        assertEquals(201, resp.status());
        Map<String, Object> body = parseResponseBody(resp);
        assertEquals("Cadastro realizado com sucesso", body.get("message"));
        assertNotNull(body.get("_id"));
    }

    @Test
    @DisplayName("CT12 - Prevent deleting a product that is part of a cart")
    void ct12_preventDeletingProductInCart() throws Exception {
        String adminToken = getAdminToken();

        String productName = "Product " + System.currentTimeMillis();
        String productPayload = String.format(
                "{\n" +
                        "  \"nome\": \"%s\",\n" +
                        "  \"preco\": 300,\n" +
                        "  \"descricao\": \"Product linked to cart\",\n" +
                        "  \"quantidade\": 10\n" +
                        "}",
                productName);

        APIResponse createProductResp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", adminToken)
                .setData(productPayload));
        assertEquals(201, createProductResp.status());

        Map<String, Object> createProductBody = objectMapper.readValue(createProductResp.body(), Map.class);
        String productId = (String) createProductBody.get("_id");

        // Create non-admin user and login
        String userEmail = "cart.user." + System.currentTimeMillis() + "@example.com";
        String userPassword = "SenhaSegura@123";

        String userData = String.format(
                "{\n" +
                        "  \"nome\": \"Cart User\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"%s\",\n" +
                        "  \"administrador\": \"false\"\n" +
                        "}",
                userEmail, userPassword);

        request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(userData));

        String loginPayload = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", userEmail, userPassword);
        APIResponse loginResp = request.post("/login", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(loginPayload));
        assertEquals(200, loginResp.status());

        Map<String, Object> loginBody = objectMapper.readValue(loginResp.body(), Map.class);
        String userToken = (String) loginBody.get("authorization");

        // Cancel any existing cart
        request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", userToken));

        // Create cart with the product
        String cartBody = String.format(
                "{\n" +
                        "  \"produtos\": [{\"idProduto\": \"%s\", \"quantidade\": 1}]\n" +
                        "}",
                productId);
        APIResponse cartResp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", userToken)
                .setData(cartBody));
        assertEquals(201, cartResp.status());

        // Try to delete product that is in a cart
        APIResponse deleteResp = request.delete("/produtos/" + productId, RequestOptions.create()
                .setHeader("Authorization", adminToken));
        assertEquals(400, deleteResp.status());

        Map<String, Object> deleteBody = objectMapper.readValue(deleteResp.body(), Map.class);
        assertEquals("Não é permitido excluir produto que faz parte de carrinho", deleteBody.get("message"));
    }

    @Test
    @DisplayName("CT13 - Restrict product creation to administrators only")
    void ct13_restrictProductCreationToAdmins() throws Exception {
        // Create non-admin user
        String userEmail = "non.admin." + System.currentTimeMillis() + "@example.com";
        String userPassword = "SenhaSegura@123";

        String userData = String.format(
                "{\n" +
                        "  \"nome\": \"Non Admin User\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"%s\",\n" +
                        "  \"administrador\": \"false\"\n" +
                        "}",
                userEmail, userPassword);

        request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(userData));

        // Login as non-admin
        String loginPayload = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", userEmail, userPassword);
        APIResponse loginResp = request.post("/login", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(loginPayload));
        assertEquals(200, loginResp.status());

        Map<String, Object> loginBody = objectMapper.readValue(loginResp.body(), Map.class);
        String nonAdminToken = (String) loginBody.get("authorization");

        // Try to create product with non-admin token
        String productData = "{\n" +
                "  \"nome\": \"Restricted Product\",\n" +
                "  \"preco\": 500,\n" +
                "  \"descricao\": \"Product should be created only by admins\",\n" +
                "  \"quantidade\": 5\n" +
                "}";

        APIResponse productResp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", nonAdminToken)
                .setData(productData));

        assertEquals(403, productResp.status());
        Map<String, Object> productBody = objectMapper.readValue(productResp.body(), Map.class);
        assertEquals("Rota exclusiva para administradores", productBody.get("message"));
    }
}
