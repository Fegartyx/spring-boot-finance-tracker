package dev.artyx.finance_tracker.controller;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.model.transaction.CreateTransactionRequest;
import dev.artyx.finance_tracker.model.transaction.TransactionResponse;
import dev.artyx.finance_tracker.model.transaction.UpdateTransactionRequest;
import dev.artyx.finance_tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping(path = "/api/transaction", produces = "application/json", consumes = "application/json")
    public WebResponse<String> createTransaction(User user, @RequestBody CreateTransactionRequest request) {
        transactionService.create(user, request);
        return WebResponse.<String>builder()
                .data("Transaction created successfully")
                .build();
    }

    @PutMapping(path = "/api/transaction/{transactionId}", produces = "application/json", consumes = "application/json")
    public WebResponse<String> updateTransaction(User user, @RequestBody UpdateTransactionRequest request, @PathVariable UUID transactionId) {
        transactionService.edit(user, request);
        return WebResponse.<String>builder()
                .data("Transaction updated successfully")
                .build();
    }

    @DeleteMapping(path = "/api/transaction/{transactionId}", produces = "application/json")
    public WebResponse<String> deleteTransaction(User user, @PathVariable UUID transactionId) {
        transactionService.delete(user, transactionId);
        return WebResponse.<String>builder()
                .data("Transaction deleted successfully")
                .build();
    }

    @GetMapping(path = "/api/transaction/{transactionId}", produces = "application/json")
    public WebResponse<TransactionResponse> getTransaction(User user, @PathVariable UUID transactionId) {
        TransactionResponse response = transactionService.get(user, transactionId);
        return WebResponse.<TransactionResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping(path = "/api/transactions", produces = "application/json")
    public WebResponse<List<TransactionResponse>> getAllTransactions(User user) {
        List<TransactionResponse> responses = transactionService.getAll(user);
        return WebResponse.<List<TransactionResponse>>builder().data(responses).build();
    }
}
