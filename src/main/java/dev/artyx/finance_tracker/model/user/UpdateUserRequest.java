package dev.artyx.finance_tracker.model.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(max = 100)
    private String username;

    @Size(max = 100)
    private String password;

    @Size(max = 255)
    private String email;
}
