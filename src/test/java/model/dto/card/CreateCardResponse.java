package model.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCardResponse {
    private String cardHolder;
    private String cardNumber;
    private String expiredDate;
}
