package dev.artyx.finance_tracker.model.transaction;

import dev.artyx.finance_tracker.entity.Category;
import dev.artyx.finance_tracker.entity.Wallet;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTransactionRequest {

    @Size(max = 255)
    private String description;

    @NotNull(message = "Amount is required")
    @Size(max = 255)
    private Long amount;

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    @NotNull
    private Category category;

    @NotNull
    private Wallet wallet;
}
