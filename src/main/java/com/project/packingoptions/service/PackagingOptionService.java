package com.project.packingoptions.service;

import com.project.packingoptions.dto.PackagingOptionRequest;
import com.project.packingoptions.model.PackagingOption;

import java.util.List;
import java.util.Optional;

public interface PackagingOptionService {
    List<PackagingOption> getAllPackagingOptions();
    Optional<PackagingOption> getPackagingOptionById(Long id);
    List<PackagingOption> getPackagingOptionsByProductCode(String productCode);
    PackagingOption createPackagingOption(PackagingOptionRequest request);
    PackagingOption updatePackagingOption(Long id, PackagingOptionRequest request);
    void deletePackagingOption(Long id);

}
