package com.project.packingoptions.service;

import com.project.packingoptions.dto.PackagingOptionRequest;
import com.project.packingoptions.exception.ResourceNotFoundException;
import com.project.packingoptions.model.PackagingOption;
import com.project.packingoptions.model.Product;
import com.project.packingoptions.repository.PackagingOptionRepository;
import com.project.packingoptions.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PackagingOptionServiceImpl implements PackagingOptionService {

    private final PackagingOptionRepository packagingOptionRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PackagingOption> getAllPackagingOptions() {
        log.info("Retrieving all packaging options");
        return packagingOptionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PackagingOption> getPackagingOptionById(Long id) {
        log.info("Retrieving packaging option with ID: {}", id);
        return packagingOptionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackagingOption> getPackagingOptionsByProductCode(String productCode) {
        log.info("Retrieving packaging options for product: {}", productCode);

        if (!productRepository.existsByCode(productCode)) {
            throw new ResourceNotFoundException("Product", "code", productCode);
        }

        return packagingOptionRepository.findByProductCode(productCode);
    }

    @Override
    public PackagingOption createPackagingOption(PackagingOptionRequest request) {
        log.info("Creating packaging option for product: {}", request.getProductCode());

        Product product = productRepository.findByCode(request.getProductCode())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "code", request.getProductCode()));

        PackagingOption packagingOption = PackagingOption.builder()
                .product(product)
                .productCode(request.getProductCode())
                .bundleSize(request.getBundleSize())
                .bundlePrice(request.getBundlePrice())
                .build();

        return packagingOptionRepository.save(packagingOption);
    }

    @Override
    public PackagingOption updatePackagingOption(Long id, PackagingOptionRequest request) {
        log.info("Updating packaging option with ID: {}", id);

        PackagingOption existingOption = packagingOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackagingOption", "id", id));

        Product product = productRepository.findByCode(request.getProductCode())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "code", request.getProductCode()));

        existingOption.setProduct(product);
        existingOption.setBundleSize(request.getBundleSize());
        existingOption.setBundlePrice(request.getBundlePrice());

        return packagingOptionRepository.save(existingOption);
    }

    @Override
    public void deletePackagingOption(Long id) {
        log.info("Deleting packaging option with ID: {}", id);

        if (!packagingOptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("PackagingOption", "id", id);
        }

        packagingOptionRepository.deleteById(id);
    }
}


