package com.example.orders.menu;

import com.example.orders.TestFactory;
import com.example.orders.exception.ResourceNotFoundException;
import com.example.orders.menu.dto.ImageUploadUrlResponse;
import com.example.orders.menu.dto.MenuItemRequest;
import com.example.orders.menu.dto.MenuItemResponse;
import com.example.orders.menu.mapper.MenuItemMapper;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.model.MenuItem;
import com.example.orders.menu.repository.MenuCategoryRepository;
import com.example.orders.menu.repository.MenuItemRepository;
import com.example.orders.menu.service.MenuItemService;
import com.example.orders.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

  @Mock
  MenuItemRepository itemRepository;
  @Mock
  MenuCategoryRepository categoryRepository;
  @Mock
  MenuItemMapper itemMapper;
  @Mock
  StorageService storageService;

  @InjectMocks
  MenuItemService itemService;

  @Test
  void getAll_returnsAllItems() {
    MenuItem item = TestFactory.menuItem(TestFactory.menuCategory());
    MenuItemResponse response = TestFactory.menuItemResponse(item);

    when(itemRepository.findAll()).thenReturn(List.of(item));
    when(itemMapper.toResponse(item)).thenReturn(response);

    List<MenuItemResponse> responseItems = itemService.getAll();

    assertThat(responseItems).isEqualTo(List.of(response));
  }

  @Test
  void getById_returnsItem() {
    MenuItem item = TestFactory.menuItem(TestFactory.menuCategory());
    MenuItemResponse response = TestFactory.menuItemResponse(item);

    when(itemRepository.findById(any())).thenReturn(Optional.of(item));
    when(itemMapper.toResponse(item)).thenReturn(response);

    MenuItemResponse itemResponse = itemService.getById(item.getId());

    assertThat(itemResponse).isEqualTo(response);
  }

  @Test
  void getById_throwsWhenNotFound() {
    when(itemRepository.findById(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> itemService.getById(UUID.randomUUID())).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void create_savesAndReturnsItem() {
    MenuCategory category = TestFactory.menuCategory();
    UUID userId = UUID.randomUUID();
    MenuItemRequest request = TestFactory.menuItemRequest(category.getId());
    MenuItem item =TestFactory.menuItem(category);
    MenuItemResponse response = TestFactory.menuItemResponse(item);

    when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
    when(itemMapper.toEntity(request, category, userId)).thenReturn(item);
    when(itemRepository.save(item)).thenReturn(item);
    when(itemMapper.toResponse(item)).thenReturn(response);

    MenuItemResponse result = itemService.create(request, userId);

    assertThat(result).isEqualTo(response);
    verify(itemRepository).save(item);
  }

  @Test
  void create_throwsWhenCategoryNotFound() {
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.randomUUID());
    when(categoryRepository.findById(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> itemService.create(request, UUID.randomUUID())).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void update_updatesAndReturnsItem() {
    MenuCategory category = TestFactory.menuCategory();
    UUID userId = UUID.randomUUID();
    MenuItemRequest request = TestFactory.menuItemRequest(category.getId());
    MenuItem item =TestFactory.menuItem(category);
    MenuItemResponse response = TestFactory.menuItemResponse(item);

    when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
    when(itemMapper.toResponse(item)).thenReturn(response);

    MenuItemResponse result = itemService.update(item.getId(), request, userId);

    assertThat(result).isEqualTo(response);
    verify(itemMapper).updateEntity(request, category, userId, item);
  }

  @Test
  void update_throwsWhenItemNotFound() {
    UUID userId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.randomUUID());
    when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> itemService.update(itemId, request, userId)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void update_throwsWhenCategoryNotFound() {
    MenuCategory category = TestFactory.menuCategory();
    MenuItem item = TestFactory.menuItem(category);
    UUID userId = UUID.randomUUID();
    MenuItemRequest request = TestFactory.menuItemRequest(UUID.randomUUID());
    when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    when(categoryRepository.findById(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> itemService.update(item.getId(), request, userId)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void delete_deletesItem() {
    UUID itemId = UUID.randomUUID();
    when(itemRepository.existsById(itemId)).thenReturn(true);
    itemService.delete(itemId);
    verify(itemRepository).deleteById(itemId);
  }

  @Test
  void delete_throwsWhenNotFound() {
    UUID itemId = UUID.randomUUID();
    when(itemRepository.existsById(itemId)).thenReturn(false);
    assertThatThrownBy(() -> itemService.delete(itemId)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void generateImageUploadUrl_returnsUploadUrl() {
    MenuCategory category = TestFactory.menuCategory();
    MenuItem item = TestFactory.menuItem(category);
    String contentType = "image/jpeg";
    String key = "menu/images/key.jpg";
    String presignedUrl = "https://minio/bucket/presigned-upload-url";

    when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    when(storageService.generateKey(item.getId(), contentType)).thenReturn(key);
    when(storageService.buildPublicUrl(key)).thenReturn("https://minio/bucket/" + key);
    when(storageService.generatePresignedUploadUrl(key)).thenReturn(presignedUrl);

    ImageUploadUrlResponse result = itemService.generateImageUploadUrl(item.getId(), contentType);

    assertThat(result.uploadUrl()).isEqualTo(presignedUrl);
    verify(storageService).generateKey(item.getId(), contentType);
  }
}

