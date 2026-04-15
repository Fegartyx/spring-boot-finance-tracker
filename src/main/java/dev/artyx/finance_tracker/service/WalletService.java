package dev.artyx.finance_tracker.service;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.entity.Wallet;
import dev.artyx.finance_tracker.model.wallet.CreateWalletRequest;
import dev.artyx.finance_tracker.model.wallet.UpdateWalletRequest;
import dev.artyx.finance_tracker.model.wallet.WalletResponse;
import dev.artyx.finance_tracker.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final ValidationService validationService;
    private final WalletRepository walletRepository;

    private WalletResponse toWalletResponse(Wallet wallet) {
        return WalletResponse.builder().name(wallet.getName()).balance(wallet.getBalance()).build();
    }

    @Transactional
    public void create(User user, CreateWalletRequest request) {
        validationService.validate(request);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setName(request.getName());
        wallet.setBalance(request.getBalance());
        walletRepository.save(wallet);
    }

    @Transactional
    public void edit(User user, UpdateWalletRequest request) {
        validationService.validate(request);

        Wallet wallet = walletRepository.findByUserAndId(user, request.getWalletId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        if (request.getBalance() != null) {
            wallet.setBalance(request.getBalance());
        }
        if (request.getName() != null) {
            wallet.setName(request.getName());
        }
        walletRepository.save(wallet);
    }

    @Transactional
    public void delete(User user, UUID id) {
        Wallet wallet = walletRepository.findByUserAndId(user, id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        walletRepository.delete(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getAllWallets(User user) {
        List<Wallet> wallet = walletRepository.findAllByUser(user);
        return wallet.stream().map(this::toWalletResponse).toList();
    }

    @Transactional(readOnly = true)
    public WalletResponse get(User user, UUID id) {
        Wallet wallet = walletRepository.findByUserAndId(user, id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        return toWalletResponse(wallet);
    }
}
