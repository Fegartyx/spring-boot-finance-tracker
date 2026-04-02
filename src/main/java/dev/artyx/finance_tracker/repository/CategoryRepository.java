package dev.artyx.finance_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.artyx.finance_tracker.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
