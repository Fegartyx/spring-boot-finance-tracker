package dev.artyx.finance_tracker.service;

import dev.artyx.finance_tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ValidationService validationService;
}
