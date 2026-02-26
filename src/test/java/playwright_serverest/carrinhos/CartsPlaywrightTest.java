package playwright_serverest.carrinhos;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

import playwright_serverest.BaseApiTest;
import playwright_serverest.utils.FakerUtils;

@SuppressWarnings("unchecked")
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public class CartsPlaywrightTest extends BaseApiTest {

    private String loginWithDefaultPayload() throws Exception {
        String userEmail = FakerUtils.randomEmail();
        String userPassword = "SenhaSegura@123";

        String newUser = String.format("""
        {
        "nome": "Cart Default User",
        "email": "%s",
        "password": "%s",
        "administrador": "true"
        }
        """, userEmail, userPassword);

        request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(newUser));

        String loginPayload = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", userEmail, userPassword);

        APIResponse resp = request.post("/login", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(loginPayload));
        assertEquals(200, resp.status());

        Map<String, Object> loginBody = objectMapper.readValue(resp.body(), Map.class);
        return (String) loginBody.get("authorization");
    }

    private String createAdminUserAndGetToken() throws Exception {
        String userEmail = FakerUtils.randomEmail();
        String userPassword = "SenhaSegura@123";

        String newUser = String.format("""
                {
                  "nome": "Cart User",
                  "email": "%s",
                  "password": "%s",
                  "administrador": "true"
                }
                """, userEmail, userPassword);

        request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(newUser));

        String loginPayload = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", userEmail, userPassword);

        APIResponse resp = request.post("/login", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(loginPayload));
        assertEquals(200, resp.status());

        Map<String, Object> loginBody = objectMapper.readValue(resp.body(), Map.class);
        return (String) loginBody.get("authorization");
    }

    private String createProduct(String token, int price, int quantity, String description) throws Exception {
        String productName = FakerUtils.randomProduct();

        String productData = String.format("""
                {
                  "nome": "%s",
                  "preco": %d,
                  "descricao": "%s",
                  "quantidade": %d
                }
                """, productName, price, description, quantity);

        APIResponse productResp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(productData));
        assertEquals(201, productResp.status());

        Map<String, Object> productBody = objectMapper.readValue(productResp.body(), Map.class);
        assertEquals("Cadastro realizado com sucesso", productBody.get("message"));
        return (String) productBody.get("_id");
    }

    @Test
    @DisplayName("CT01 - Full cart lifecycle for authenticated user")
    void ct01_fullCartLifecycleForAuthenticatedUser() throws Exception {
        String token = createAdminUserAndGetToken();

        // Cancel any existing cart
        request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", token));

        String productId = createProduct(token, 150, 10, "Product created for cart lifecycle test");

        String cartBody = String.format("{\"produtos\":[{\"idProduto\":\"%s\",\"quantidade\":2}]}", productId);

        APIResponse createCartResp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(cartBody));
        assertEquals(201, createCartResp.status());

        Map<String, Object> createCartBody = objectMapper.readValue(createCartResp.body(), Map.class);
        assertEquals("Cadastro realizado com sucesso", createCartBody.get("message"));
        assertNotNull(createCartBody.get("_id"));

        String cartId = (String) createCartBody.get("_id");

        APIResponse getCartResp = request.get("/carrinhos/" + cartId);
        assertEquals(200, getCartResp.status());

        Map<String, Object> getCartBody = objectMapper.readValue(getCartResp.body(), Map.class);

        java.util.List<?> produtos = (java.util.List<?>) getCartBody.get("produtos");
        assertEquals(1, produtos.size());
        assertNotNull(getCartBody.get("precoTotal"));
        assertNotNull(getCartBody.get("quantidadeTotal"));
        assertNotNull(getCartBody.get("idUsuario"));
        assertEquals(cartId, getCartBody.get("_id"));

        APIResponse concludeResp = request.delete("/carrinhos/concluir-compra", RequestOptions.create()
                .setHeader("Authorization", token));
        assertEquals(200, concludeResp.status());

        Map<String, Object> concludeBody = objectMapper.readValue(concludeResp.body(), Map.class);
        String message = (String) concludeBody.get("message");
        assertTrue(message.contains("Registro excluído com sucesso"));
    }

    @Test
    @DisplayName("CT02 - Cancel purchase and return products to stock")
    void ct02_cancelPurchaseAndReturnProductsToStock() throws Exception {
        String token = loginWithDefaultPayload();

        // Cancel any existing cart
        request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", token));

        String productId = createProduct(token, 200, 5, "Product for cancel purchase test");

        String cartBody = String.format("{\"produtos\":[{\"idProduto\":\"%s\",\"quantidade\":1}]}", productId);

        APIResponse createCartResp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(cartBody));
        assertEquals(201, createCartResp.status());

        APIResponse cancelResp = request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", token));
        assertEquals(200, cancelResp.status());

        Map<String, Object> cancelBody = objectMapper.readValue(cancelResp.body(), Map.class);
        assertNotNull(cancelBody.get("message"));
    }

    @Test
    @DisplayName("CT03 - Prevent creating cart without authentication token")
    void ct03_preventCreatingCartWithoutAuthenticationToken() throws Exception {
        String cartBody = "{\"produtos\":[{\"idProduto\":\"BeeJh5lz3k6kSIzA\",\"quantidade\":1}]}";

        APIResponse resp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(cartBody));

        assertEquals(401, resp.status());
        Map<String, Object> body = parseResponseBody(resp);
        assertEquals("Token de acesso ausente, inválido, expirado ou usuário do token não existe mais",
                body.get("message"));
    }

    @Test
    @DisplayName("CT04 - Prevent creating more than one cart for the same user")
    void ct04_preventCreatingMoreThanOneCartForSameUser() throws Exception {
        String token = loginWithDefaultPayload();

        // Cancel any existing cart
        request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", token));

        String productId = createProduct(token, 120, 3, "Product for multiple cart test");

        String firstCart = String.format("{\"produtos\":[{\"idProduto\":\"%s\",\"quantidade\":1}]}", productId);

        APIResponse firstResp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(firstCart));
        assertEquals(201, firstResp.status());

        APIResponse secondResp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(firstCart));
        assertEquals(400, secondResp.status());

        Map<String, Object> secondBody = objectMapper.readValue(secondResp.body(), Map.class);
        assertTrue(((String) secondBody.get("message")).contains("Não é permitido ter mais de 1 carrinho"));
    }

    @Test
    @DisplayName("CT05 - Cart not found by ID")
    void ct05_cartNotFoundById() throws Exception {
        APIResponse resp = request.get("/carrinhos/invalid-cart-id-123");
        assertEquals(400, resp.status());

        Map<String, Object> body = parseResponseBody(resp);
        assertEquals("id deve ter exatamente 16 caracteres alfanuméricos", body.get("id"));
    }

    @Test
    @DisplayName("CT06 - Prevent cart creation when product stock is insufficient")
    void ct06_preventCartCreationWhenProductStockIsInsufficient() throws Exception {
        String token = loginWithDefaultPayload();

        // Cancel any existing cart
        request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", token));

        String productId = createProduct(token, 100, 1, "Low stock product for cart test");

        String cartBody = String.format("{\"produtos\":[{\"idProduto\":\"%s\",\"quantidade\":2}]}", productId);

        APIResponse resp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(cartBody));

        assertEquals(400, resp.status());
        Map<String, Object> body = parseResponseBody(resp);
        assertTrue(((String) body.get("message")).contains("Produto não possui quantidade suficiente"));
    }

    @Test
    @DisplayName("CT07 - Prevent cart creation with duplicated products in the same cart")
    void ct07_preventCartCreationWithDuplicatedProductsInSameCart() throws Exception {
        String token = loginWithDefaultPayload();

        // Cancel any existing cart
        request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", token));

        String productId = createProduct(token, 150, 10, "Product created for duplicated products cart test");

        String duplicatedCartBody = String.format(
                "{\"produtos\":[{\"idProduto\":\"%s\",\"quantidade\":1},{\"idProduto\":\"%s\",\"quantidade\":1}]}",
                productId, productId);

        APIResponse resp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(duplicatedCartBody));

        assertEquals(400, resp.status());
        Map<String, Object> body = parseResponseBody(resp);
        assertTrue(((String) body.get("message")).contains("Não é permitido possuir produto duplicado"));
    }

    @Test
    @DisplayName("CT08 - Prevent cart creation with non-existing product")
    void ct08_preventCartCreationWithNonExistingProduct() throws Exception {
        String token = loginWithDefaultPayload();

        // Cancel any existing cart
        request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", token));

        String invalidCartBody = "{\"produtos\":[{\"idProduto\":\"AAAAAAAAAAAAAAAA\",\"quantidade\":1}]}";

        APIResponse resp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", token)
                .setData(invalidCartBody));

        assertEquals(400, resp.status());
        Map<String, Object> body = parseResponseBody(resp);
        assertTrue(((String) body.get("message")).contains("Produto não encontrado"));
    }
}
