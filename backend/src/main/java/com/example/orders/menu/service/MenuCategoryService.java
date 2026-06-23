package com.example.orders.menu.service;

import com.example.orders.exception.ResourceNotFoundException;
import com.example.orders.menu.dto.CategoryRequest;
import com.example.orders.menu.dto.CategoryResponse;
import com.example.orders.menu.dto.PositionEntry;
import com.example.orders.menu.mapper.MenuCategoryMapper;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuCategoryService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuCategoryMapper categoryMapper;

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAllByOrderByPositionAsc().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request, UUID currentUserId) {
        MenuCategory entity = categoryMapper.toEntity(request, currentUserId);
        int nextPosition = categoryRepository.findTopByOrderByPositionDesc()
                .map(c -> c.getPosition() + 1)
                .orElse(0);
        entity.setPosition(nextPosition);
        return categoryMapper.toResponse(categoryRepository.save(entity));
    }

    @Transactional
    public CategoryResponse update(UUID categoryId, CategoryRequest request, UUID currentUserId) {
        MenuCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        categoryMapper.updateEntity(request, currentUserId, category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Transactional
    public void updatePositions(List<PositionEntry> entries) {
        Map<UUID, MenuCategory> categories = categoryRepository.findAllById(
                entries.stream().map(PositionEntry::id).toList()
        ).stream().collect(Collectors.toMap(MenuCategory::getId, c -> c));

        entries.forEach(entry -> {
            MenuCategory category = categories.get(entry.id());
            if (category != null) {
                category.setPosition(entry.position());
            }
        });

        categoryRepository.saveAll(categories.values());
    }
}
