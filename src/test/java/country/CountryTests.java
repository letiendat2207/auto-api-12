package country;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import model.Country;
import model.CountryPagination;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
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

public class CountryTests {
    private static final String GET_COUNTRIES_API = "/api/v1/countries";
    private static final String GET_COUNTRY_API = "/api/v1/countries/{code}";
    private static final String GET_COUNTRY_WITH_FILTER_API = "/api/v3/countries";
    private static final String GET_COUNTRY_WITH_PAGINATION_API = "/api/v4/countries";
    private static final String GET_COUNTRY_WITH_HEADER_API = "/api/v5/countries";

    private static final String X_POWERED_BY_HEADER = "X-Powered-By";
    private static final String X_POWERED_BY_HEADER_VALUE = "Express";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json; charset=utf-8";
    private static final String API_KEY_HEADER = "api-key";
    private static final String API_KEY_HEADER_VALUE = "private";

    private static final String GDP_FILTER = "gdp";
    private static final String OPERATOR_FILTER = "operator";
    private static final String PAGE = "page";
    private static final String SIZE = "size";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifySchemaOfGetCountriesApi() {
        RestAssured.given().log().all()
                .get(GET_COUNTRIES_API)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/countries-schema.json"));
    }

    @Test
    void verifyCountriesApiData() throws JsonProcessingException {
        Response response = RestAssured.given().log().all()
                .get(GET_COUNTRIES_API);
        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        ObjectMapper mapper = new ObjectMapper();
        List<Country> expected = mapper.readValue(CountriesData.ALL_COUNTRIES_DATA, new TypeReference<List<Country>>() {
        });
        List<Country> actual = response.body().as(new TypeRef<List<Country>>() {
        });
        assertThat(actual.size(), equalTo(expected.size()));
        assertThat(actual.containsAll(expected), equalTo(true));
        assertThat(expected.containsAll(actual), equalTo(true));
    }

    @Test
    void verifySchemaOfGetCountryApi() {
        RestAssured.given().log().all()
                .get(GET_COUNTRIES_API + "/VN")
                .then()
                .log().all()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/country-schema.json"));
    }

    static Stream<Country> countryProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> inputData = mapper.readValue(CountriesData.ALL_COUNTRIES_DATA, new TypeReference<>() {
        });
        return inputData.stream();
    }

    @ParameterizedTest
    @MethodSource("countryProvider")
    void verifyGetCountry(Country input) throws JsonProcessingException {

        Response response = RestAssured.given().log().all()
                .get(String.format(GET_COUNTRY_API), input.getCode());
        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        Country actual = response.body().as(Country.class);
        assertThat(actual, equalToObject(input));
    }

    @Test
    void verifySchemaOfGetCountryApiWithFilter() {
        RestAssured.given().log().all()
                .queryParam(GDP_FILTER, 5000)
                .queryParam(OPERATOR_FILTER, ">")
                .get(GET_COUNTRY_WITH_FILTER_API)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/country-with-filter-schema.json"));
    }

    @Test
    void verifyGetCountryApiWithFilterGreaterThan() {
        Response response = RestAssured.given().log().all()
                .queryParam(GDP_FILTER, 5000)
                .queryParam(OPERATOR_FILTER, ">")
                .get(GET_COUNTRY_WITH_FILTER_API);

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        List<Country> actual = response.body().as(new TypeRef<>() {
        });

        for (Country country : actual) {
            assertThat(country.getGdp(), greaterThan(5000f));
        }
    }

    static Stream<Arguments> getCountryWithFilterProvider() {
        return Stream.of(
                Arguments.of(">", 5000, greaterThan(5000f)),
                Arguments.of(">=", 5000, greaterThanOrEqualTo(5000f)),
                Arguments.of("<", 5000, lessThan(5000f)),
                Arguments.of("<=", 5000, lessThanOrEqualTo(5000f)),
                Arguments.of("==", 5000, equalTo(5000f))
        );
    }

    @ParameterizedTest
    @MethodSource("getCountryWithFilterProvider")
    void verifyGetCountryApiWithFilter(String operator, int gdp, Matcher expected) {
        Response response = RestAssured.given().log().all()
                .queryParam(GDP_FILTER, gdp)
                .queryParam(OPERATOR_FILTER, operator)
                .get(GET_COUNTRY_WITH_FILTER_API);

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        List<Country> actual = response.body().as(new TypeRef<>() {
        });

        for (Country country : actual) {
            assertThat(country.getGdp(), expected);
        }
    }

    @Test
    void verifySchemaOfGetCountryApiWithPagination() {
        RestAssured.given().log().all()
                .queryParam(PAGE, 1)
                .queryParam(SIZE, 4)
                .get(GET_COUNTRY_WITH_PAGINATION_API)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/country-pagination-schema.json"));
    }

    @Test
    void verifyCountryApiDataWithPagination() throws JsonProcessingException {
        int testSize = 4;

        Response response = getCountryApiWithPagination(testSize, 1);
        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify headers
        response.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        CountryPagination actualDataFirstPage = response.body().as(CountryPagination.class);
        verifyPage(actualDataFirstPage, testSize, 1, testSize);

        // 4. get second page
        response = getCountryApiWithPagination(testSize, 2);

        response.then().log().all().statusCode(200);
        CountryPagination actualDataSecondPage = response.body().as(CountryPagination.class);
        verifyPage(actualDataSecondPage, testSize, 2, testSize);

        // 5. Verify data from first page vs second page
        assertThat(actualDataFirstPage.getData().containsAll(actualDataSecondPage.getData()), equalTo(false));
        assertThat(actualDataSecondPage.getData().containsAll(actualDataFirstPage.getData()), equalTo(false));

        // 6. Verify last page
        int lastPage = actualDataSecondPage.getTotal() / testSize;
        int sizeOfLastPage = actualDataSecondPage.getTotal() % testSize;
        if (sizeOfLastPage != 0) {
            lastPage++;
        } else {
            sizeOfLastPage = testSize;
        }

        response = getCountryApiWithPagination(testSize, lastPage);
        response.then().log().all().statusCode(200);
        CountryPagination actualDataLastPage = response.body().as(CountryPagination.class);
        verifyPage(actualDataLastPage, testSize, lastPage, sizeOfLastPage);
    }

    private static void verifyPage(CountryPagination pageData, int expectedSize, int expectedPage, int expectedLength) {
        assertThat(pageData.getPage(), equalTo(expectedPage));
        assertThat(pageData.getSize(), equalTo(expectedSize));
        assertThat(pageData.getData(), hasSize(expectedLength));
    }

    private static Response getCountryApiWithPagination(int testSize, int page) {
        Response response = RestAssured.given().log().all()
                .queryParam(PAGE, page)
                .queryParam(SIZE, testSize)
                .get(GET_COUNTRY_WITH_PAGINATION_API);
        return response;
    }

    @Test
    void verifySchemaOfGetCountryApiWithHeaders() {
        RestAssured.given().log().all()
                .header(API_KEY_HEADER, API_KEY_HEADER_VALUE)
                .get(GET_COUNTRY_WITH_HEADER_API)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("json-schema/country-header-schema.json"));
    }

}
