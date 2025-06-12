package testCase.user;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.login.LoginRequest;
import model.login.LoginResponse;
import model.user.*;
import org.junit.jupiter.api.Test;
import testCase.TestMaster;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static utils.ConstantUtils.*;
import static utils.DateTimeUtils.parseTimeToCurrentTimeZone;
import static utils.DateTimeUtils.verifyDateTime;

public class CreateUserTest extends TestMaster {
    @Test
    void verifyCreateUserSuccessful() {
        // Create User Address
        UserAddressRequest userAddressRequest = new UserAddressRequest();
        userAddressRequest.setStreetNumber("123");
        userAddressRequest.setStreet("Main St");
        userAddressRequest.setWard("Ward 1");
        userAddressRequest.setDistrict("District 1");
        userAddressRequest.setCity("Thu Duc");
        userAddressRequest.setState("Ho Chi Minh");
        userAddressRequest.setZip("70000");
        userAddressRequest.setCountry("VN");

        // Create user
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("Jos");
        userRequest.setLastName("Doe");
        userRequest.setMiddleName("Smith");
        userRequest.setBirthday("01-23-2000");
        userRequest.setEmail(String.format("auto_api_%s@abc.com", System.currentTimeMillis()));
        userRequest.setPhone("01234567890");
        userRequest.setAddresses(List.of(userAddressRequest));

        LocalDateTime timeBeforeCreate = LocalDateTime.now();

        Response createUserResponse = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, String.format("Bearer %s", token))
                .body(userRequest)
                .post(CREATE_USER_API);

        // 1. Verify status code
        createUserResponse.then().log().all().statusCode(200);

        // 2. Verify headers and schema
        createUserResponse.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(RESPONSE_CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        UserResponse userResponse = createUserResponse.body().as(UserResponse.class);
        assertThat(userResponse.getId(), not(emptyOrNullString()));
        assertThat(userResponse.getMessage(), equalTo("Customer created"));

        // 4. Double check that user existing in the system or not by getUserApi
        Response getUserResponse = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, String.format("Bearer %s", token))
                .get(GET_USER_API, userResponse.getId());

        LocalDateTime timeAfterCreate = LocalDateTime.now();
        // 5. Verify status
        getUserResponse.then().log().all().statusCode(200);

        // 6. Verify get user response again request
        GetUserResponse actualGetUserResponse = getUserResponse.body().as(GetUserResponse.class);
        assertThat(actualGetUserResponse.getId(), equalTo(userResponse.getId()));
        assertThat(actualGetUserResponse, jsonEquals(userRequest).whenIgnoringPaths(
                "id", "createdAt", "updatedAt", "addresses[*].id",
                "addresses[*].customerId", "addresses[*].createdAt", "addresses[*].updatedAt"));

        LocalDateTime userCreateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getCreatedAt());
        verifyDateTime(timeBeforeCreate, timeAfterCreate, userCreateAtDate);

        LocalDateTime userUpdateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getUpdatedAt());
        verifyDateTime(timeBeforeCreate, timeAfterCreate, userUpdateAtDate);

        for (GetUserAddressResponse addressResponse : actualGetUserResponse.getAddresses()) {
            assertThat(addressResponse.getCustomerId(), equalTo(userResponse.getId()));
            assertThat(addressResponse.getId(), not(emptyOrNullString()));

            LocalDateTime userAddressCreateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getCreatedAt());
            verifyDateTime(timeBeforeCreate, timeAfterCreate, userCreateAtDate);

            LocalDateTime userAddressUpdateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getUpdatedAt());
            verifyDateTime(timeBeforeCreate, timeAfterCreate, userUpdateAtDate);

        }
    }

    @Test
    void verifyCreateUserSuccessfulWithMultipleAddress() {

        // Create User Address
        UserAddressRequest userAddressRequest = new UserAddressRequest();
        userAddressRequest.setStreetNumber("123");
        userAddressRequest.setStreet("Main St");
        userAddressRequest.setWard("Ward 1");
        userAddressRequest.setDistrict("District 1");
        userAddressRequest.setCity("Thu Duc");
        userAddressRequest.setState("Ho Chi Minh");
        userAddressRequest.setZip("70000");
        userAddressRequest.setCountry("VN");

        // Create user
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("Jos");
        userRequest.setLastName("Doe");
        userRequest.setMiddleName("Smith");
        userRequest.setBirthday("01-23-2000");
        userRequest.setEmail(String.format("auto_api_%s@abc.com", System.currentTimeMillis()));
        userRequest.setPhone("01234567890");
        userRequest.setAddresses(List.of(userAddressRequest));

        LocalDateTime timeBeforeCreate = LocalDateTime.now();

        Response createUserResponse = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, String.format("Bearer %s", token))
                .body(userRequest)
                .post(CREATE_USER_API);

        // 1. Verify status code
        createUserResponse.then().log().all().statusCode(200);

        // 2. Verify headers and schema
        createUserResponse.then().header(X_POWERED_BY_HEADER, equalTo(X_POWERED_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(RESPONSE_CONTENT_TYPE_HEADER_VALUE));

        // 3. Verify body
        UserResponse userResponse = createUserResponse.body().as(UserResponse.class);
        assertThat(userResponse.getId(), not(emptyOrNullString()));
        assertThat(userResponse.getMessage(), equalTo("Customer created"));

        // 4. Double check that user existing in the system or not by getUserApi
        Response getUserResponse = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, String.format("Bearer %s", token))
                .get(GET_USER_API, userResponse.getId());

        LocalDateTime timeAfterCreate = LocalDateTime.now();
        // 5. Verify status
        getUserResponse.then().log().all().statusCode(200);

        // 6. Verify get user response again request
        GetUserResponse actualGetUserResponse = getUserResponse.body().as(GetUserResponse.class);
        assertThat(actualGetUserResponse.getId(), equalTo(userResponse.getId()));
        assertThat(actualGetUserResponse, jsonEquals(userRequest).whenIgnoringPaths(
                "id", "createdAt", "updatedAt", "addresses[*].id",
                "addresses[*].customerId", "addresses[*].createdAt", "addresses[*].updatedAt"));

        LocalDateTime userCreateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getCreatedAt());
        verifyDateTime(timeBeforeCreate, timeAfterCreate, userCreateAtDate);

        LocalDateTime userUpdateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getUpdatedAt());
        verifyDateTime(timeBeforeCreate, timeAfterCreate, userUpdateAtDate);

        for (GetUserAddressResponse addressResponse : actualGetUserResponse.getAddresses()) {
            assertThat(addressResponse.getCustomerId(), equalTo(userResponse.getId()));
            assertThat(addressResponse.getId(), not(emptyOrNullString()));

            LocalDateTime userAddressCreateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getCreatedAt());
            verifyDateTime(timeBeforeCreate, timeAfterCreate, userCreateAtDate);

            LocalDateTime userAddressUpdateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getUpdatedAt());
            verifyDateTime(timeBeforeCreate, timeAfterCreate, userUpdateAtDate);
        }
    }
}
