package dev.artyx.finance_tracker.model.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.artyx.finance_tracker.entity.TransactionType;
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
public class UpdateCategoryRequest {

    @JsonIgnore
    @NotNull
    private Long categoryId;

    @Size(max = 200)
    private String name;

    private TransactionType type;
}
