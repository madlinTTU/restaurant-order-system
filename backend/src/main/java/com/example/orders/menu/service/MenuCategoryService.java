package com.example.orders.menu.service;

import com.example.orders.exception.ResourceNotFoundException;
import com.example.orders.menu.dto.CategoryRequest;
import com.example.orders.menu.dto.CategoryResponse;
import com.example.orders.menu.mapper.MenuCategoryMapper;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional (readOnly = true)
public class MenuCategoryService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuCategoryMapper categoryMapper;

    @Cacheable("menuCategories")
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "menuCategories", allEntries = true)
    public CategoryResponse create(CategoryRequest request, UUID currentUserId) {
        return categoryMapper.toResponse(
                categoryRepository.save(categoryMapper.toEntity(request, currentUserId))
        );
    }

    @Transactional
    @CacheEvict(value = "menuCategories", allEntries = true)
    public CategoryResponse update(UUID id, CategoryRequest request, UUID currentUserId) {
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        categoryMapper.updateEntity(request, currentUserId, category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    @CacheEvict(value = "menuCategories", allEntries = true)
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
