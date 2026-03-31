package dev.artyx.finance_tracker.model.wallet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateWalletRequest {

    @JsonIgnore
    @NotNull
    private UUID walletId;

    @Size(max = 255)
    private String name;

    private Long balance;
}
