package dev.artyx.finance_tracker.model.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.artyx.finance_tracker.entity.Category;
import dev.artyx.finance_tracker.entity.Wallet;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTransactionRequest {

    @JsonIgnore
    @NotNull
    private UUID transactionId;

    @Size(max = 255)
    private String description;

    @Size(max = 255)
    private Long amount;

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    @NotNull
    private Category category;

    @NotNull
    private Wallet wallet;
}
