package login;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import country.CountriesData;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.country.Country;
import model.login.LoginError;
import model.login.LoginRequest;
import model.login.LoginResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoginTest {
    private static final String LOGIN_API = "/api/login";

    private static final String X_POWERED_BY_HEADER = "X-Powered-By";
    private static final String X_POWERED_BY_HEADER_VALUE = "Express";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String REQUEST_CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String RESPONSE_CONTENT_TYPE_HEADER_VALUE = "application/json; charset=utf-8";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifySchemaOfLoginApi() {
        LoginRequest loginRequest = new LoginRequest("staff", "1234567890");

        RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .body(loginRequest)
                .post(LOGIN_API)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/login-schema.json"));
    }

    @Test
    void verifyLoginSuccessful() {
        LoginRequest loginRequest = new LoginRequest("staff", "1234567890");

        Response response = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .body(loginRequest)
                .post(LOGIN_API);

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify header
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(RESPONSE_CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        LoginResponse actual = response.body().as(LoginResponse.class);
        assertThat(actual.getToken(), not(emptyOrNullString()));
        assertThat(actual.getTimeout(), equalTo(120000));
    }

    static Stream<Arguments> loginProvider() {
        return Stream.of(
                Arguments.of(new LoginRequest("", "1234567890")),
                Arguments.of(new LoginRequest("$%^45", "1234567890")),
                Arguments.of(new LoginRequest(null, "1234567890")),
                Arguments.of(new LoginRequest("staff", "")),
                Arguments.of(new LoginRequest("staff", null)),
                Arguments.of(new LoginRequest("staff", "123"))
        );
    }

    @ParameterizedTest
    @MethodSource("loginProvider")
    void verifyLoginFail(LoginRequest loginRequest) {

        Response response = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .body(loginRequest)
                .post(LOGIN_API);

        // 1. Verify status code
        response.then().log().all().statusCode(401);

        // 2. Verify header
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(RESPONSE_CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        LoginError actual = response.body().as(LoginError.class);
        assertThat(actual.getMessage(), equalTo("Invalid credentials"));
    }
}
