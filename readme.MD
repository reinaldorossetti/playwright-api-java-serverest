# 🧪 Playwright API Testing - ServeRest

[![Playwright](https://img.shields.io/badge/Playwright-Java-45ba63.svg)](https://playwright.dev/java/)
[![JUnit5](https://img.shields.io/badge/JUnit-5.10.1-green.svg)](https://junit.org/junit5/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)

Projeto de automação de testes de API utilizando **Microsoft Playwright para Java** para testar a API REST **ServeRest** - uma API gratuita que simula uma loja virtual.

Projeto estruturado com boas práticas, exemplos de testes, validações avançadas e integração com JUnit 5 para execução e relatórios.

URI do repositório: [https://github.com/reinaldorossetti/karate_api_java](https://github.com/reinaldorossetti/karate_api_java)

Reporte na esteira: [https://reinaldorossetti.github.io/karate_api_java/allure-reports/index.html](https://reinaldorossetti.github.io/karate_api_java/allure-reports/index.html)

Testes realizados na API: [TESTING_API.MD](TESTING_API.MD)

---

## 📚 Índice

- [Sobre o Playwright](#-sobre-o-playwright)
- [Sobre a API ServeRest](#-sobre-a-api-serverest)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Executando os Testes](#-executando-os-testes)
- [Exemplos de Testes](#-exemplos-de-testes)
- [Funcionalidades do Playwright](#-funcionalidades-do-playwright)
- [Relatórios](#-relatórios)
- [Boas Práticas](#-boas-práticas)
- [Recursos Adicionais](#-recursos-adicionais)

---

## 🧪 Sobre o Playwright

**Playwright** é um framework open-source da Microsoft, conhecido por testes E2E em browser e também por uma API robusta para testes HTTP via `APIRequestContext`, ideal para automação de APIs em Java.

### ✨ Principais Características

- **🔥 APIRequestContext fluente**: Requisições HTTP com `get`, `post`, `put`, `delete`
- **🚀 Suporte completo a REST**: operações GET, POST, PUT, PATCH e DELETE com JSON
- **🎯 Assertions com JUnit 5**: validações claras e objetivas
- **🔄 Contexto reutilizável**: configuração de `baseURL`, headers e ciclo de vida centralizado
- **📊 Integração com Allure**: relatórios de execução com JUnit 5
- **🧪 Data-Driven Testing**: suporte com `@CsvFileSource`, `@ValueSource`, `@ParameterizedTest`
- **⚡ Execução Paralela**: suporte via JUnit 5 (`@Execution(CONCURRENT)`) + Surefire
- **🔐 Autenticação**: suporte total para tokens e headers customizados

### 💡 Por que usar Playwright para API?

1. **Padronização**: mesmo framework para API e UI quando necessário
2. **Confiabilidade**: cliente HTTP moderno e estável
3. **Integração**: funciona naturalmente com Maven + JUnit 5
4. **Evolução**: ecossistema ativo e muito bem documentado
5. **CI/CD**: fácil integração com GitHub Actions

---

## 🌐 Sobre a API ServeRest

[ServeRest](https://serverest.dev/) é uma API REST gratuita que simula uma loja virtual para fins educacionais e prática de testes de API.

### 🛍️ Endpoints Disponíveis

| Recurso | Endpoints | Descrição |
|---------|-----------|-----------|
| **Login** | `POST /login` | Autenticação de usuários |
| **Usuários** | `GET, POST, PUT, DELETE /usuarios` | Gerenciamento de usuários |
| **Produtos** | `GET, POST, PUT, DELETE /produtos` | Gerenciamento de produtos (requer admin) |
| **Carrinhos** | `GET, POST, DELETE /carrinhos` | Gerenciamento de carrinhos de compras |

### 🔗 Base URL

```
https://serverest.dev
```

### 📖 Documentação Completa

- **Swagger UI**: https://serverest.dev/
- **Repositório**: https://github.com/ServeRest/ServeRest
- **Front-end (Beta)**: https://front.serverest.dev/

---

## 📁 Estrutura do Projeto

```
karate_api_java/
│
├── src/
│   ├── main/
│   │   └── java/                         # (vazio neste projeto)
│   │
│   └── test/
│       ├── java/
│       │   └── playwright_serverest/
│       │       ├── BaseApiTest.java
│       │       ├── ExecutionBuilderRunner.java
│       │       ├── login/
│       │       │   └── LoginPlaywrightTest.java
│       │       ├── usuarios/
│       │       │   └── UsersPlaywrightTest.java
│       │       ├── produtos/
│       │       │   └── ProductsPlaywrightTest.java
│       │       ├── carrinhos/
│       │       │   └── CartsPlaywrightTest.java
│       │       └── utils/
│       │           └── FakerUtils.java
│       │
│       └── resources/
│           ├── playwright/
│           │   └── login/
│           │       ├── invalid-login-emails.csv
│           │       └── invalido-login.csv
│           └── serverest/
│               ├── usuarios/resources/userPayload.json
│               └── produtos/resources/productPayload.json
│
├── pom.xml                               # Dependências Maven
└── readme-playwright.MD                  # Este arquivo
```

---

## 🔧 Pré-requisitos

- **Java JDK 21** (LTS) ou superior
- **Maven 3.6+**
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)
- Conexão com internet (para acessar a API ServeRest)

### Download do Java 21

- Oracle JDK 21: https://www.oracle.com/java/technologies/downloads/#java21

### Verificar instalação

```bash
java -version
mvn -version
```

---

## 📦 Versões das dependências (pom.xml)

| Componente                         | Artefato                                           | Versão  |
|------------------------------------|----------------------------------------------------|---------|
| Playwright Java                    | com.microsoft.playwright:playwright                | 1.49.0  |
| JUnit 5 (Jupiter API)             | org.junit.jupiter:junit-jupiter-api                | 5.10.1  |
| JUnit 5 (Jupiter Engine)          | org.junit.jupiter:junit-jupiter-engine             | 5.10.1  |
| JUnit 5 (Jupiter Params)          | org.junit.jupiter:junit-jupiter-params             | 5.10.1  |
| Jackson Databind                   | com.fasterxml.jackson.core:jackson-databind        | 2.17.0  |
| Java Faker                         | com.github.javafaker:javafaker                     | 1.0.2   |
| Hamcrest                           | org.hamcrest:hamcrest                              | 2.2     |
| Maven Surefire Plugin             | org.apache.maven.plugins:maven-surefire-plugin     | 3.2.5   |
| Allure JUnit5                      | io.qameta.allure:allure-junit5                     | 2.17.0  |

---

## 🚀 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/reinaldorossetti/karate_api_java.git
cd karate_api_java
```

### 2. Instale as dependências

```bash
mvn -q test-compile
mvn clean install
```

### 3. Verifique a instalação

```bash
mvn test
```

---

## ▶️ Executando os Testes

### Executar suíte completa Playwright

```bash
mvn clean -Dtest=playwright_serverest.ExecutionBuilderRunner test
```

### Executar suíte de login

```bash
mvn clean -Dtest=playwright_serverest.login.LoginPlaywrightTest test
```

### Executar método específico

```bash
mvn clean -Dtest=playwright_serverest.login.LoginPlaywrightTest#ct01_loginWithValidCredentials test
```

### Execução paralela (JUnit 5)

Arquivo: `junit-platform.properties`

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=2
```

---

## ⚙️ Esteira CI/CD - GitHub Actions

Este projeto possui integração contínua configurada no GitHub Actions em:

- Arquivo: `.github/workflows/api-tests.yml`

### 🔁 Quando a esteira é executada

- `push` em qualquer branch
- `pull_request` aberto ou atualizado

### 🧱 Passos principais do job `test`

1. **Checkout do código**
   - `actions/checkout@v4`
2. **Configuração do Java 21**
   - `actions/setup-java@v5` com:
     - `java-version: '21'`
     - `distribution: 'temurin'`
3. **Execução dos testes via Maven**
   - Comando sugerido para Playwright API: `mvn clean -Dtest=playwright_serverest.ExecutionBuilderRunner test`
4. **Publicação de relatórios Allure**
   - Publica `target/allure-report` via `peaceiris/actions-gh-pages@v4` na branch `gh-pages`, pasta `allure-reports`.

### 🌐 Acesso ao relatório no GitHub Pages

Após a execução da esteira:

- URL do relatório:
  - `https://reinaldorossetti.github.io/karate_api_java/allure-reports/index.html`

---

## 📝 Exemplos de Testes

### Exemplo 1: Configuração Base (BaseApiTest)

```java
public abstract class BaseApiTest {

    protected static final String BASE_URL = "https://serverest.dev";
    protected Playwright playwright;
    protected APIRequestContext request;

    @BeforeAll
    void setupPlaywright() {
        playwright = Playwright.create();
        request = playwright.request().newContext(
            new APIRequest.NewContextOptions().setBaseURL(BASE_URL)
        );
    }

    @AfterAll
    void teardownPlaywright() {
        if (request != null) request.dispose();
        if (playwright != null) playwright.close();
    }
}
```

### Exemplo 2: Login com sucesso

```java
@Test
@DisplayName("CT01 - Perform login with valid credentials and validate token")
void ct01_loginWithValidCredentials() throws Exception {
    String email = FakerUtils.randomEmail();
    String password = "SenhaSegura@123";

    request.post("/usuarios", RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData("{\"nome\":\"Teste\",\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"administrador\":\"true\"}"));

    APIResponse loginResp = request.post("/login", RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));

    assertEquals(200, loginResp.status());
}
```

### Exemplo 3: Login inválido

```java
@Test
@DisplayName("CT02 - Attempt login with invalid credentials")
void ct02_loginWithInvalidCredentials() throws Exception {
    APIResponse resp = request.post("/login", RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData("{\"email\":\"invalido@teste.com\",\"password\":\"senhaerrada\"}"));

    assertEquals(401, resp.status());
}
```

### Exemplo 4: Validação de campos obrigatórios

```java
@Test
@DisplayName("CT03 - Validate required fields on login")
void ct03_validateRequiredFields() {
    APIResponse resp = request.post("/login", RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData("{\"email\":\"\",\"password\":\"senha123\"}"));

    assertEquals(400, resp.status());
}
```

### Exemplo 5: Uso do token em rota protegida

```java
@Test
@DisplayName("CT04 - Login and use token to access a protected resource")
void ct04_loginAndUseTokenInProtectedRoute() throws Exception {
    String token = "...";

    APIResponse resp = request.post("/produtos", RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setHeader("Authorization", token)
        .setData("{\"nome\":\"Produto Teste\",\"preco\":100,\"descricao\":\"Teste\",\"quantidade\":10}"));

    assertEquals(403, resp.status());
}
```

### Exemplo 6: Teste parametrizado com CSV

```java
@ParameterizedTest(name = "CT05 - Email inválido: {0}")
@CsvFileSource(resources = "/playwright/login/invalid-login-emails.csv", numLinesToSkip = 1)
@Execution(ExecutionMode.CONCURRENT)
@DisplayName("CT05 - Validate invalid email format")
void ct05_validateInvalidEmailFormat(String invalidEmail) {
    APIResponse resp = request.post("/login", RequestOptions.create()
        .setHeader("Content-Type", "application/json")
        .setData("{\"email\":\"" + invalidEmail + "\",\"password\":\"senha123\"}"));

    assertEquals(400, resp.status());
}
```

### Arquivo CSV utilizado no CT05

`src/test/resources/playwright/login/invalid-login-emails.csv`

```csv
email
emailwithoutat
@noname.com
email@nodomain
email
12345@test.c
!@#$%
```

---

## 🎯 Funcionalidades do Playwright

### 1. Contexto reutilizável de API

```java
APIRequestContext request = playwright.request().newContext(
    new APIRequest.NewContextOptions().setBaseURL("https://serverest.dev")
);
```

### 2. Requisições HTTP com RequestOptions

```java
APIResponse response = request.post("/login", RequestOptions.create()
    .setHeader("Content-Type", "application/json")
    .setData(payload));
```

### 3. Extração e parsing de resposta

```java
Map<String, Object> body = objectMapper.readValue(response.body(), Map.class);
String token = (String) body.get("authorization");
int status = response.status();
```

### 4. Variáveis e Reutilização

```java
String email = FakerUtils.randomEmail();
String product = FakerUtils.randomProduct();

String payload = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
```

### 5. Hooks e Setup com JUnit 5

```java
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public class LoginPlaywrightTest extends BaseApiTest {
    // setup e teardown herdados de BaseApiTest
}
```

---

## 📊 Relatórios

Após executar os testes, relatórios Allure podem ser gerados em:

```
target/allure-report/index.html
```

Para gerar e abrir o relatório localmente:

```bash
mvn allure:serve
```

O relatório exibe:
- ✅ Testes executados, passados e falhos
- ⏱️ Tempo de execução por teste
- 📋 Resultado de requisições e respostas
- 📊 Histórico de execuções

---

## 🎓 Boas Práticas

### 1. Organização das Classes de Teste

- ✅ Uma classe por recurso (`LoginPlaywrightTest`, `UsersPlaywrightTest`)
- ✅ Use `BaseApiTest` para configurações comuns
- ✅ Nomeie os métodos de forma descritiva com prefixo `ct0X_`

### 2. Anotações para Organização

```java
@Tag("smoke")
@Tag("regression")
@Tag("login")
@Disabled
```

### 3. Reutilização de Código

- ✅ Crie métodos helper (ex: `createUser()`, `getAdminToken()`, `createProduct()`)
- ✅ Centralize setup e teardown em `BaseApiTest`
- ✅ Centralize geração de dados no `FakerUtils`

### 4. Dados de Teste

```java
String email = FakerUtils.randomEmail();
String product = FakerUtils.randomProduct() + System.currentTimeMillis();
```

### 5. Validações Completas

```java
assertEquals(200, response.status());
assertNotNull(body.get("authorization"));
assertEquals("Login realizado com sucesso", body.get("message"));
```

---

## 📚 Recursos Adicionais

### Documentação Oficial

- 🎭 **Playwright Java**: https://playwright.dev/java/docs/intro
- 🌐 **ServeRest**: https://serverest.dev/
- ☕ **JUnit 5**: https://junit.org/junit5/
- 📊 **Allure**: https://docs.qameta.io/allure/

### Tutoriais e Cursos

- [Playwright Java APIRequestContext](https://playwright.dev/java/docs/api/class-apirequestcontext)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [ServeRest GitHub](https://github.com/ServeRest/ServeRest)

### Comunidade

- [Stack Overflow - Playwright Tag](https://stackoverflow.com/questions/tagged/playwright)
- [Playwright GitHub Issues](https://github.com/microsoft/playwright/issues)

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👨‍💻 Autor

Desenvolvido para fins de estudo e prática de automação de testes de API.

---

## ⭐ Agradecimentos

- **Microsoft** - Criadora do Playwright
- **Paulo Gonçalves** - Criador da API ServeRest
- Comunidade open-source

---

Referências:
- https://playwright.dev/java/docs/intro
- https://junit.org/junit5/
- https://github.com/microsoft/playwright

**🚀 Happy Testing with Playwright!** 🎭
