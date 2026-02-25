package playwright_serverest.utils;

import com.github.javafaker.Faker;

/**
 * Utility class to provide random test data using Java Faker.
 */
public class FakerUtils {

    private static final Faker FAKER = new Faker();

    public static String randomName() {
        return FAKER.name().fullName();
    }

    public static String randomProduct() {
        return FAKER.commerce().productName();
    }

    public static String randomEmail() {
        return FAKER.name().firstName().toLowerCase() + "@gmail.com";
    }

    public static String randomPassword() {
        // minLength 8, maxLength 16 for reasonably strong test passwords
        return FAKER.internet().password(8, 16);
    }
}
