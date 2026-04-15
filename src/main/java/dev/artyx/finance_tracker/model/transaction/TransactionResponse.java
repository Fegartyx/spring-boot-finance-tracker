package dev.artyx.finance_tracker.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private String description;
    private Long amount;
    private LocalDateTime date;
    private String categoryName;
    private String walletName;
}
