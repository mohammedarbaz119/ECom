package com.test.commerce.repositories;

import com.test.commerce.model.Product;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> fetchCategoryandRetailerIfPresent(Long retailerId,Long categoryId) {
        return (root, query, builder) -> {
            if (!Long.class.equals(query.getResultType())) {
                if (categoryId != null) {
                    root.fetch("category", JoinType.LEFT);
                }
                if (retailerId != null) {
                    root.fetch("retailer", JoinType.LEFT);
                }
                query.distinct(true);
            }
            return builder.conjunction();
        };
    }

    public static Specification<Product> hasretailer(Long retailerId){
        return (root, query, builder) ->
                retailerId == null ? null : builder.equal(root.get("retailer").get("id"), retailerId);
    }
    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, builder) ->
                categoryId == null ? null : builder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasNameLike(String name) {
        return (root, query, builder) ->
                name == null || name.isBlank() ? null : builder.or(builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"),builder.like(builder.lower(root.get("description")), "%" + name.toLowerCase() + "%"));
    }

    public static Specification<Product> isInStock(Boolean inStock) {
        return (root, query, builder) -> {
            if (inStock == null) return null;
            return inStock
                    ? builder.greaterThan(root.get("stock"), 0)
                    : builder.equal(root.get("stock"), 0);
        };
    }
}
