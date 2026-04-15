package dev.artyx.finance_tracker.service;

import dev.artyx.finance_tracker.entity.Transaction;
import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.transaction.CreateTransactionRequest;
import dev.artyx.finance_tracker.model.transaction.TransactionResponse;
import dev.artyx.finance_tracker.model.transaction.UpdateTransactionRequest;
import dev.artyx.finance_tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ValidationService validationService;

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder().amount(transaction.getAmount()).description(transaction.getDescription()).date(transaction.getDate()).categoryName(transaction.getCategory().getName()).walletName(transaction.getWallet().getName()).build();
    }

    @Transactional
    public void create(User user, CreateTransactionRequest createTransactionRequest) {
        validationService.validate(createTransactionRequest);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setWallet(createTransactionRequest.getWallet());
        transaction.setCategory(createTransactionRequest.getCategory());
        transaction.setAmount(createTransactionRequest.getAmount());
        transaction.setDescription(createTransactionRequest.getDescription());
        transaction.setDate(createTransactionRequest.getDate());

        transactionRepository.save(transaction);
    }

    @Transactional
    public void edit(User user, UpdateTransactionRequest updateTransactionRequest) {
        validationService.validate(updateTransactionRequest);

        Transaction transaction = transactionRepository.findByUserAndId(user, updateTransactionRequest.getTransactionId()).orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (updateTransactionRequest.getAmount() != null) transaction.setAmount(updateTransactionRequest.getAmount());
        if (updateTransactionRequest.getDescription() != null)
            transaction.setDescription(updateTransactionRequest.getDescription());
        updateTransactionRequest.setWallet(transaction.getWallet());
        updateTransactionRequest.setCategory(transaction.getCategory());
        updateTransactionRequest.setDate(transaction.getDate());
        transactionRepository.save(transaction);
    }

    @Transactional
    public void delete(User user, UUID transactionId) {
        Transaction transaction = transactionRepository.findByUserAndId(user, transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(User user, UUID transactionId) {
        Transaction transaction = transactionRepository.findByUserAndId(user, transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));
        return toTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAll(User user) {
        List<Transaction> transactions = transactionRepository.findAllByUser(user);
        return transactions.stream().map(this::toTransactionResponse).toList();
    }
}
