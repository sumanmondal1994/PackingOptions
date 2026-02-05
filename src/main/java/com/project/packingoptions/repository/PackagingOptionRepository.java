package com.project.packingoptions.repository;


import com.project.packingoptions.model.PackagingOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackagingOptionRepository extends JpaRepository<PackagingOption, Long> {

    List<PackagingOption> findByProductCode(String productCode);

    void deleteByProductCode(String productCode);
}
