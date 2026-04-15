package dev.artyx.finance_tracker.controller;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.model.wallet.CreateWalletRequest;
import dev.artyx.finance_tracker.model.wallet.UpdateWalletRequest;
import dev.artyx.finance_tracker.model.wallet.WalletResponse;
import dev.artyx.finance_tracker.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping(path = "/api/wallet", consumes = "application/json", produces = "application/json")
    public WebResponse<String> create(@AuthenticationPrincipal User user, @RequestBody CreateWalletRequest request) {
        walletService.create(user, request);
        return WebResponse.<String>builder()
                .data("Wallet created successfully")
                .build();
    }

    @PutMapping(path = "/api/wallet/{walletId}", consumes = "application/json", produces = "application/json")
    public WebResponse<String> edit(@AuthenticationPrincipal User user, @RequestBody UpdateWalletRequest request, @PathVariable UUID walletId) {
        request.setWalletId(walletId);
        walletService.edit(user, request);
        return WebResponse.<String>builder()
                .data("Wallet updated successfully")
                .build();
    }

    @DeleteMapping(path = "/api/wallet/{walletId}", produces = "application/json")
    public WebResponse<String> delete(@AuthenticationPrincipal User user, @PathVariable UUID walletId) {
        walletService.delete(user, walletId);
        return WebResponse.<String>builder()
                .data("Wallet deleted successfully")
                .build();
    }

    @GetMapping(path = "/api/wallet/{walletId}", produces = "application/json")
    public WebResponse<WalletResponse> get(@AuthenticationPrincipal User user, @PathVariable UUID walletId) {
        WalletResponse response = walletService.get(user, walletId);
        return WebResponse.<WalletResponse>builder().data(response).build();
    }

    @GetMapping(path = "/api/wallets", produces = "application/json")
    public WebResponse<List<WalletResponse>> getAllWallets(@AuthenticationPrincipal User user) {
        List<WalletResponse> responses = walletService.getAllWallets(user);
        return WebResponse.<List<WalletResponse>>builder().data(responses).build();
    }
}
