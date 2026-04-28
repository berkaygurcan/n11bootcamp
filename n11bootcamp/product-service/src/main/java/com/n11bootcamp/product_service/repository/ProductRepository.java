package com.n11bootcamp.product_service.repository;


import com.n11bootcamp.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query(value = "UPDATE public.product SET category_key = :newKey WHERE LOWER(TRIM(category_key)) = LOWER(TRIM(:oldKey))", nativeQuery = true)
    int updateCategoryKeyForProducts(@Param("oldKey") String oldKey, @Param("newKey") String newKey);


}
