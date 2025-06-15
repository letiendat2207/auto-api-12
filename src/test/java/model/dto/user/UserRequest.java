package model.dto.user;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    private List<UserAddressRequest> addresses;

    public static UserRequest getDefault(){
        return UserRequest.builder()
                .firstName("Jos")
                .lastName("Doe")
                .middleName("Smith")
                .birthday("01-23-2000")
                .email("Thu Duc")
                .phone("01234567890")
                .addresses(new ArrayList<>())
                .build();
    }
}
