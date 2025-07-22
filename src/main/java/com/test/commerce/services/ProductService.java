package com.test.commerce.services;

import com.test.commerce.dtos.CustomerProductData;
import com.test.commerce.dtos.ProductCreationDto;
import com.test.commerce.dtos.ProductData;
import com.test.commerce.model.Category;
import com.test.commerce.model.Product;
import com.test.commerce.model.Retailer;
import com.test.commerce.model.User;
import com.test.commerce.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ProductService {
    private final Logger logger = Logger.getLogger(ProductService.class.getName());

    @Autowired
    private RetailorRepository retailorRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private S3UploadService s3UploadService;

    @Autowired
    private CategoryRepository categoryRepository;

    public Product AddProduct(ProductCreationDto productCreationDto, String email) {
        Retailer retailor = retailorRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("retailer not found"));

        Category category = categoryRepository.findByName(productCreationDto.getCategory()).orElseGet(() -> new Category(productCreationDto.getCategory()));
        categoryRepository.save(category);

        logger.info(category.getName());
        Product product = new Product();
        product.setName(productCreationDto.getName());
        product.setCost(new BigDecimal(productCreationDto.getCost()));
        product.setMrp(new BigDecimal(productCreationDto.getMrp()));
        product.setDescription(productCreationDto.getDescription());
        product.setStock(Integer.valueOf(productCreationDto.getStock()));
        product.setCategory(category);
        product.setImageUrl(productCreationDto.getImageUrl());
        product.setRetailer(retailor);

        return productRepository.save(product);
    }

    public List<ProductData> getProducts(Long RetailerId, boolean inStock) {
        List<ProductData> data = productRepository.findAllByRetailorWithCategoryName(RetailerId, inStock);
        return data.stream().map(l -> {
            if (l.getImageUrl() != null && !(l.getImageUrl().isEmpty())) {
                l.setImageUrl(s3UploadService.getObjectUrl(l.getImageUrl()));
            }
            return l;
        }).toList();
    }

    public Page<CustomerProductData> searchProducts(Long retailerId, Long categoryId, String searchTerm, Boolean inStock, String sortBy, int page, int size) {
        Specification<Product> spec = Specification.allOf(ProductSpecification.fetchCategoryandRetailerIfPresent(retailerId, categoryId)).and(ProductSpecification.hasretailer(retailerId)).and(ProductSpecification.hasCategoryId(categoryId)).and(ProductSpecification.hasNameLike(searchTerm)).and(ProductSpecification.isInStock(inStock));

        Sort sort = switch (sortBy) {
            case "priceAsc" -> Sort.by("mrp").ascending();
            case "priceDesc" -> Sort.by("mrp").descending();
            case "dateAsc" -> Sort.by("createdAt").ascending();
            case "dateDesc" -> Sort.by("createdAt").descending();
            default -> Sort.by("createdAt").descending(); // default
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        var a = productRepository.findAll(spec, pageable);
        return a.map(l -> new CustomerProductData(l.getId(), l.getName(), l.getRetailer().getRetailerName(), l.getDescription(), l.getStock(), l.getMrp(), l.getCategory().getName(), l.getCreatedAt(), l.getImageUrl() != null ? s3UploadService.getObjectUrl(l.getImageUrl()) : null));
    }

    public Optional<CustomerProductData> getProduct(Long id) {
        return productRepository.findProjectedById(id);
    }
}
