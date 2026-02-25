package playwright_serverest;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * JUnit 5 suite to execute in parallel via Surefire
 * the Playwright tests for Login, Users, Products and Carts.
 *
 * To run only this suite:
 *   mvn test -Dtest=playwright_serverest.ExecutionBuilderRunner
 */
@Suite
@SuiteDisplayName("Run Playwright Serverest API Tests")
@SelectClasses({
    playwright_serverest.login.LoginPlaywrightTest.class,
    playwright_serverest.usuarios.UsersPlaywrightTest.class,
    playwright_serverest.produtos.ProductsPlaywrightTest.class,
    playwright_serverest.carrinhos.CartsPlaywrightTest.class
})
public class ExecutionBuilderRunner {
}
