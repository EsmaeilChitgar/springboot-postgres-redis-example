package com.example.redis.springbootrediscache.controller;

import com.example.redis.springbootrediscache.exception.ResourceNotFoundException;
import com.example.redis.springbootrediscache.model.Product;
import com.example.redis.springbootrediscache.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/{productId}")
    @Cacheable(value = "products",key = "#productId",unless = "#result.price > 200")
    public Product findProductById(@PathVariable(value = "productId") Integer productId) {
        System.out.println("Employee fetching from database:: "+productId);
        return productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Employee not found" + productId));

    }

    @PutMapping("/{productId}")
    @CachePut(value = "products",key = "#productId")
    public Product updateProduct(@PathVariable(value = "productId") Integer productId,
                                 @RequestBody Product productDetails) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for this id :: " + productId));
        product.setName(productDetails.getName());
        product.setPrice(productDetails.getPrice());
        return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(@PathVariable(value = "id") Integer productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product not found" + productId));
        productRepository.delete(product);
    }
}
