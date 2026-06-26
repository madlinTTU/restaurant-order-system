package com.example.orders.menu.service;

import com.example.orders.exception.ResourceNotFoundException;
import com.example.orders.menu.dto.ImageUploadUrlResponse;
import com.example.orders.menu.dto.MenuItemRequest;
import com.example.orders.menu.dto.MenuItemResponse;
import com.example.orders.menu.dto.PositionEntry;
import com.example.orders.menu.mapper.MenuItemMapper;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.model.MenuItem;
import com.example.orders.menu.repository.MenuCategoryRepository;
import com.example.orders.menu.repository.MenuItemRepository;
import com.example.orders.storage.StorageService;
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
public class MenuItemService {

  private final MenuItemRepository itemRepository;
  private final MenuCategoryRepository categoryRepository;
  private final MenuItemMapper itemMapper;
  private final StorageService storageService;

  public List<MenuItemResponse> getAll() {
    return itemRepository.findAllByOrderByCategoryIdAscPositionAsc().stream()
      .map(itemMapper::toResponse)
      .toList();
  }

  public MenuItemResponse getById(UUID id) {
    return itemRepository.findById(id)
      .map(itemMapper::toResponse)
      .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
  }

  @Transactional
  public MenuItemResponse create(MenuItemRequest request, UUID currentUserId) {
    MenuCategory category = findCategory(request.categoryId());
    int nextPosition = itemRepository.findTopByCategoryIdOrderByPositionDesc(category.getId())
      .map(item -> item.getPosition() + 1)
      .orElse(0);
    MenuItem entity = itemMapper.toEntity(request, category, nextPosition, currentUserId);
    return itemMapper.toResponse(itemRepository.save(entity));
  }

  @Transactional
  public MenuItemResponse update(UUID itemId, MenuItemRequest request, UUID currentUserId) {
    MenuItem item = itemRepository.findById(itemId)
      .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemId));
    MenuCategory category = findCategory(request.categoryId());
    itemMapper.updateEntity(request, category, currentUserId, item);
    if (request.available() != null) item.setAvailable(request.available());
    return itemMapper.toResponse(item);
  }

  @Transactional
  public void delete(UUID id) {
    if (!itemRepository.existsById(id)) {
      throw new ResourceNotFoundException("Menu item not found: " + id);
    }
    itemRepository.deleteById(id);
  }

  @Transactional
  public ImageUploadUrlResponse generateImageUploadUrl(UUID id, String contentType) {
    MenuItem item = itemRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));

    String key = storageService.generateKey(id, contentType);
    String publicUrl = storageService.buildPublicUrl(key);
    String uploadUrl = storageService.generatePresignedUploadUrl(key);

    item.setImageUrl(publicUrl);
    return new ImageUploadUrlResponse(uploadUrl);
  }

  @Transactional
  public void updatePositions(List<PositionEntry> entries) {
    Map<UUID, MenuItem> items = itemRepository.findAllById(
      entries.stream().map(PositionEntry::id).toList()
    ).stream().collect(Collectors.toMap(MenuItem::getId, i -> i));

    entries.forEach(entry -> {
      MenuItem item = items.get(entry.id());
      if (item != null) {
        item.setPosition(entry.position());
      }
    });

    itemRepository.saveAll(items.values());
  }

  private MenuCategory findCategory(UUID categoryId) {
    return categoryRepository.findById(categoryId)
      .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
  }
}
