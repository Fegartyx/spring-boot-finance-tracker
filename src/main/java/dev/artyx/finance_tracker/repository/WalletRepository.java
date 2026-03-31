package dev.artyx.finance_tracker.repository;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserAndId(User user, UUID id);
    List<Wallet> findAllByUser(User user);
}
