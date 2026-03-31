package dev.artyx.finance_tracker.model.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateWalletRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private Long balance;
}
