package com.ecommerce.productcatalogue.service;

import com.ecommerce.productcatalogue.model.Category;
import com.ecommerce.productcatalogue.model.Product;
import com.ecommerce.productcatalogue.repository.CategoryRepository;
import com.ecommerce.productcatalogue.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryName(categoryName);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + product.getCategory().getId()));
            product.setCategory(category);
        } else if (product.getCategory() != null && product.getCategory().getName() != null) {
            Category category = categoryRepository.findByName(product.getCategory().getName())
                    .orElseGet(() -> categoryRepository.save(new Category(null, product.getCategory().getName(), null)));
            product.setCategory(category);
        } else {
            throw new IllegalArgumentException("Product must be associated with a category (ID or Name).");
        }
        return productRepository.save(product);
    }

    // For Admin: Update a product
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setStockQuantity(updatedProduct.getStockQuantity());
            product.setImageUrl(updatedProduct.getImageUrl());
            if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getId() != null) {
                Category category = categoryRepository.findById(updatedProduct.getCategory().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + updatedProduct.getCategory().getId()));
                product.setCategory(category);
            }
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found for update: " + id));
    }

    // For Admin: Delete a product
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .toList();
    }
}