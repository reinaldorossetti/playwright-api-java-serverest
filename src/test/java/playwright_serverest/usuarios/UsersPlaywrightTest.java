package playwright_serverest.usuarios;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
@SuppressWarnings("unchecked")
public class UsersPlaywrightTest extends BaseApiTest {

    @Test
    @DisplayName("CT01 - List all users and validate JSON structure")
    void ct01_listAllUsersAndValidateStructure() throws Exception {
        APIResponse resp = request.get("/usuarios");

        assertEquals(200, resp.status());

        Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
        int quantidade = (int) body.get("quantidade");
        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) body.get("usuarios");

        assertTrue(quantidade > 0, "quantidade should be greater than 0");
        assertNotNull(usuarios);
        assertFalse(usuarios.isEmpty());

        for (Map<String, Object> user : usuarios) {
            assertTrue(user.containsKey("nome"));
            assertTrue(user.containsKey("email"));
            assertTrue(user.containsKey("password"));
            assertTrue(user.containsKey("administrador"));
            assertTrue(user.containsKey("_id"));

            String email = (String) user.get("email");
            assertTrue(email.matches(".+@.+\\..+"), "Email should match basic email pattern");
        }
    }

    @Test
    @DisplayName("CT02 - Get a specific user by ID")
    void ct02_getUserById() throws Exception {
        APIResponse listResp = request.get("/usuarios");
        assertEquals(200, listResp.status());

        Map<String, Object> listBody = objectMapper.readValue(listResp.body(), Map.class);
        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) listBody.get("usuarios");
        String userId = (String) usuarios.get(0).get("_id");

        APIResponse getResp = request.get("/usuarios/" + userId);
        assertEquals(200, getResp.status());

        Map<String, Object> user = objectMapper.readValue(getResp.body(), Map.class);
        assertEquals(userId, user.get("_id"));
        assertNotNull(user.get("nome"));
        assertNotNull(user.get("email"));
    }

    @Test
    @DisplayName("CT03 - Create a new user with complete validations")
    void ct03_createUser() throws Exception {
        String email = FakerUtils.randomEmail();
        String name = FakerUtils.randomName();
        String password = FakerUtils.randomPassword();

        String payload = String.format(
                "{\n" +
                        "  \"nome\": \"%s\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"%s\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}",
                name, email, password);

        APIResponse createResp = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(payload));

        assertEquals(201, createResp.status());

        Map<String, Object> createBody = objectMapper.readValue(createResp.body(), Map.class);
        assertEquals("Cadastro realizado com sucesso", createBody.get("message"));
        assertNotNull(createBody.get("_id"));

        String newUserId = (String) createBody.get("_id");

        APIResponse getResp = request.get("/usuarios/" + newUserId);
        assertEquals(200, getResp.status());

        Map<String, Object> user = objectMapper.readValue(getResp.body(), Map.class);
        assertEquals(name, user.get("nome"));
        assertEquals(email, user.get("email"));
    }

    @Test
    @DisplayName("CT04 - Advanced JSON validations with filters")
    void ct04_advancedJsonValidationsWithFilters() throws Exception {
        APIResponse resp = request.get("/usuarios");
        assertEquals(200, resp.status());

        Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) body.get("usuarios");

        List<Map<String, Object>> admins = usuarios.stream()
                .filter(u -> "true".equals(u.get("administrador")))
                .collect(Collectors.toList());
        assertTrue(admins.size() > 0, "There should be at least one admin user");

        for (Map<String, Object> user : usuarios) {
            assertNotNull(user.get("email"));
        }
    }

    @Test
    @DisplayName("CT05 - Validate error messages when creating a duplicate email")
    void ct05_duplicateEmailValidation() throws Exception {
        String duplicateEmail = FakerUtils.randomEmail();

        String user1 = String.format(
                "{\n" +
                        "  \"nome\": \"User 1\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"senha123\",\n" +
                        "  \"administrador\": \"false\"\n" +
                        "}",
                duplicateEmail);

        APIResponse first = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(user1));
        assertEquals(201, first.status());

        String user2 = String.format(
                "{\n" +
                        "  \"nome\": \"User 2\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"anotherpassword\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}",
                duplicateEmail);

        APIResponse second = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(user2));

        assertEquals(400, second.status());
        Map<String, Object> secondBody = objectMapper.readValue(second.body(), Map.class);
        assertEquals("Este email já está sendo usado", secondBody.get("message"));
        assertNotNull(secondBody.get("message"));
    }

    @Test
    @DisplayName("CT06 - Validate with fuzzy matching")
    void ct06_validateWithFuzzyMatching() throws Exception {
        APIResponse resp = request.get("/usuarios?administrador=true");

        assertEquals(200, resp.status());

        Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
        int quantidade = (int) body.get("quantidade");
        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) body.get("usuarios");

        assertTrue(quantidade >= 0);
        for (Map<String, Object> user : usuarios) {
            assertNotNull(user.get("nome"));
            assertNotNull(user.get("email"));
            assertEquals("true", String.valueOf(user.get("administrador")));
        }
    }

    @Test
    @DisplayName("CT07 - Conditional validations based on values")
    void ct07_conditionalValidationsBasedOnValues() throws Exception {
        APIResponse resp = request.get("/usuarios");
        assertEquals(200, resp.status());

        Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) body.get("usuarios");

        Map<String, Object> user = usuarios.get(0);
        String adminFlag = String.valueOf(user.get("administrador"));
        assertTrue("true".equals(adminFlag) || "false".equals(adminFlag));

        String email = (String) user.get("email");
        String password = (String) user.get("password");
        assertTrue(email != null && email.length() > 5);
        assertTrue(password != null && !password.isEmpty());
    }

    @Test
    @DisplayName("CT08 - Validate formats with regular expressions")
    void ct08_validateFormatsWithRegularExpressions() throws Exception {
        String newEmail = "test.regex." + System.currentTimeMillis() + "@example.com";

        String userData = String.format(
                "{\n" +
                        "  \"nome\": \"Regex Test\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"StrongPassword@123\",\n" +
                        "  \"administrador\": \"false\"\n" +
                        "}",
                newEmail);

        APIResponse createResp = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(userData));
        assertEquals(201, createResp.status());

        Map<String, Object> createBody = objectMapper.readValue(createResp.body(), Map.class);
        String userId = (String) createBody.get("_id");

        APIResponse getResp = request.get("/usuarios/" + userId);
        assertEquals(200, getResp.status());

        Map<String, Object> user = objectMapper.readValue(getResp.body(), Map.class);

        String email = (String) user.get("email");
        String nome = (String) user.get("nome");
        String id = (String) user.get("_id");

        assertTrue(email.matches(".+@.+\\..+"), "Email should match email pattern");
        assertTrue(nome.matches("[A-Za-z\\s]+"), "Name should contain only letters and spaces");
        assertTrue(id.matches("[A-Za-z0-9]+"), "ID should be alphanumeric");
    }

    @Test
    @DisplayName("CT09 - Validate absence of fields")
    void ct09_validateAbsenceOfFields() throws Exception {
        APIResponse resp = request.get("/usuarios");
        assertEquals(200, resp.status());

        Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
        assertNull(body.get("error"));
        assertNull(body.get("errorMessage"));

        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) body.get("usuarios");
        Map<String, Object> user = usuarios.get(0);

        assertFalse(user.containsKey("cpf"), "User should not have cpf field");
        assertFalse(user.containsKey("phone"), "User should not have phone field");
    }

    @Test
    @DisplayName("CT10 - Use variables for dynamic validations")
    void ct10_useVariablesForDynamicValidations() throws Exception {
        String expectedEmail = FakerUtils.randomEmail();

        Map<String, Object> userPayload = loadJsonResource("playwright_serverest/usuarios/resources/userPayload.json");
        userPayload.put("email", expectedEmail);

        APIResponse createResp = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(objectMapper.writeValueAsString(userPayload)));
        assertEquals(201, createResp.status());

        APIResponse searchResp = request.get("/usuarios?email=" + expectedEmail);
        assertEquals(200, searchResp.status());

        Map<String, Object> searchBody = objectMapper.readValue(searchResp.body(), Map.class);
        List<Map<String, Object>> usuarios = (List<Map<String, Object>>) searchBody.get("usuarios");

        assertFalse(usuarios.isEmpty(), "Should find the created user");
        Map<String, Object> foundUser = usuarios.get(0);
        assertEquals(expectedEmail, foundUser.get("email"));
        assertNotNull(foundUser.get("nome"));
    }

    @Test
    @DisplayName("CT11 - Prepare data for nested object validation")
    void ct11_prepareDataForNestedObjectValidation() throws Exception {
        String complexEmail = FakerUtils.randomEmail();

        String complexData = String.format(
                "{\n" +
                        "  \"nome\": \"Complex User\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"senha123\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}",
                complexEmail);

        APIResponse resp = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(complexData));
        assertEquals(201, resp.status());

        Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
        String message = (String) body.get("message");
        String id = (String) body.get("_id");

        assertNotNull(message);
        assertEquals("Cadastro realizado com sucesso", message);
        assertNotNull(id);
        assertTrue(id.length() > 10);
    }

    @Test
    @DisplayName("CT12 - Create a user from fixed JSON file")
    void ct12_createUserFromFixedJsonFile() throws Exception {
        Map<String, Object> userPayload = loadJsonResource("playwright_serverest/usuarios/resources/userPayload.json");
        userPayload.put("email", FakerUtils.randomEmail());

        APIResponse resp = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(objectMapper.writeValueAsString(userPayload)));

        assertEquals(201, resp.status());
        Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
        assertEquals("Cadastro realizado com sucesso", body.get("message"));
        assertNotNull(body.get("_id"));
    }

    @Test
    @DisplayName("CT13 - Create and delete user based on JSON payload")
    void ct13_createAndDeleteUserBasedOnJsonPayload() throws Exception {
        String expectedEmail = FakerUtils.randomEmail();
        Map<String, Object> userPayload = loadJsonResource("playwright_serverest/usuarios/resources/userPayload.json");
        userPayload.put("email", expectedEmail);

        APIResponse createResp = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(objectMapper.writeValueAsString(userPayload)));
        assertEquals(201, createResp.status());

        Map<String, Object> createBody = objectMapper.readValue(createResp.body(), Map.class);
        assertEquals("Cadastro realizado com sucesso", createBody.get("message"));
        String userId = (String) createBody.get("_id");

        APIResponse deleteResp = request.delete("/usuarios/" + userId);
        assertEquals(200, deleteResp.status());
        Map<String, Object> deleteBody = objectMapper.readValue(deleteResp.body(), Map.class);
        assertEquals("Registro excluído com sucesso", deleteBody.get("message"));

        APIResponse searchResp = request.get("/usuarios?email=" + expectedEmail);
        assertEquals(200, searchResp.status());
        Map<String, Object> searchBody = objectMapper.readValue(searchResp.body(), Map.class);
        assertEquals(0, searchBody.get("quantidade"));
    }

    @Test
    @DisplayName("CT14 - Prevent deleting user that has an associated cart")
    void ct14_preventDeletingUserThatHasAssociatedCart() throws Exception {
        String userEmail = FakerUtils.randomEmail();
        String userPassword = "SenhaSegura@123";

        String userData = String.format(
                "{\n" +
                        "  \"nome\": \"User With Cart\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"%s\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}",
                userEmail, userPassword);

        APIResponse createUserResp = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(userData));
        assertEquals(201, createUserResp.status());

        Map<String, Object> createUserBody = objectMapper.readValue(createUserResp.body(), Map.class);
        assertEquals("Cadastro realizado com sucesso", createUserBody.get("message"));
        String userId = (String) createUserBody.get("_id");

        String loginPayload = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", userEmail, userPassword);
        APIResponse loginResp = request.post("/login", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(loginPayload));
        assertEquals(200, loginResp.status());

        Map<String, Object> loginBody = objectMapper.readValue(loginResp.body(), Map.class);
        String userToken = (String) loginBody.get("authorization");

        String productName = "Product for user cart " + System.currentTimeMillis();
        String productData = String.format(
                "{\n" +
                        "  \"nome\": \"%s\",\n" +
                        "  \"preco\": 100,\n" +
                        "  \"descricao\": \"Product associated to user cart\",\n" +
                        "  \"quantidade\": 5\n" +
                        "}",
                productName);

        APIResponse productResp = request.post("/produtos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", userToken)
                .setData(productData));
        assertEquals(201, productResp.status());

        Map<String, Object> productBody = objectMapper.readValue(productResp.body(), Map.class);
        String productId = (String) productBody.get("_id");

        // Cancel any existing cart
        request.delete("/carrinhos/cancelar-compra", RequestOptions.create()
                .setHeader("Authorization", userToken));

        // Create cart
        String cartBody = String.format("{\"produtos\": [{\"idProduto\": \"%s\", \"quantidade\": 1}]}", productId);
        APIResponse cartResp = request.post("/carrinhos", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", userToken)
                .setData(cartBody));
        assertEquals(201, cartResp.status());

        // Attempt to delete user who has a cart
        APIResponse deleteResp = request.delete("/usuarios/" + userId);
        assertEquals(400, deleteResp.status());

        Map<String, Object> deleteBody = objectMapper.readValue(deleteResp.body(), Map.class);
        assertEquals("Não é permitido excluir usuário com carrinho cadastrado", deleteBody.get("message"));
        assertNotNull(deleteBody.get("idCarrinho"));
    }

    @Test
    @DisplayName("CT15 - Get user by invalid ID should return 400")
    void ct15_getUserByInvalidIdShouldReturn400() throws Exception {
        APIResponse resp = request.get("/usuarios/3F7K9P2XQ8M1R6TB");
        assertEquals(400, resp.status());

        Map<String, Object> body = objectMapper.readValue(resp.body(), Map.class);
        assertEquals("Usuário não encontrado", body.get("message"));
    }

    @Test
    @DisplayName("CT16 - Prevent updating user with duplicate e-mail")
    void ct16_preventUpdatingUserWithDuplicateEmail() throws Exception {
        String email1 = FakerUtils.randomEmail();
        String email2 = FakerUtils.randomEmail();

        String user1 = String.format(
                "{\n" +
                        "  \"nome\": \"User One\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"Senha123@\",\n" +
                        "  \"administrador\": \"false\"\n" +
                        "}",
                email1);

        String user2 = String.format(
                "{\n" +
                        "  \"nome\": \"User Two\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"Senha456@\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}",
                email2);

        APIResponse createUser1Resp = request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(user1));
        assertEquals(201, createUser1Resp.status());

        Map<String, Object> createUser1Body = objectMapper.readValue(createUser1Resp.body(), Map.class);
        String userId1 = (String) createUser1Body.get("_id");

        request.post("/usuarios", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(user2));

        String updatePayload = String.format(
                "{\n" +
                        "  \"nome\": \"User One Updated\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"Senha123@\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}",
                email2);

        APIResponse updateResp = request.put("/usuarios/" + userId1, RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(updatePayload));

        assertEquals(400, updateResp.status());
        Map<String, Object> updateBody = objectMapper.readValue(updateResp.body(), Map.class);
        assertEquals("Este email já está sendo usado", updateBody.get("message"));
    }
}
