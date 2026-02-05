package com.project.packingoptions.repository;

import com.project.packingoptions.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,String> {
    Optional<Product> findByCode(String code);
    boolean existsByCode(String code);
    void deleteByCode(String code);

}
