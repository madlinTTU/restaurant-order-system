package com.example.orders.menu;

import com.example.orders.TestFactory;
import com.example.orders.exception.ResourceNotFoundException;
import com.example.orders.menu.dto.CategoryRequest;
import com.example.orders.menu.dto.CategoryResponse;
import com.example.orders.menu.dto.PositionEntry;
import com.example.orders.menu.mapper.MenuCategoryMapper;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.repository.MenuCategoryRepository;
import com.example.orders.menu.service.MenuCategoryService;
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
class MenuCategoryServiceTest {

  @Mock
  MenuCategoryRepository categoryRepository;
  @Mock
  MenuCategoryMapper categoryMapper;

  @InjectMocks
  MenuCategoryService categoryService;

  @Test
  void getAll_returnsAllCategories() {
    MenuCategory menuCategory = TestFactory.menuCategory();
    CategoryResponse response = TestFactory.categoryResponse(menuCategory);
    when(categoryRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(menuCategory));
    when(categoryMapper.toResponse(menuCategory)).thenReturn(response);
    List<CategoryResponse> categories = categoryService.getAll();
    assertThat(categories).isEqualTo(List.of(response));
  }

  @Test
  void create_savesAndReturnsCategory() {
    CategoryRequest request = TestFactory.categoryRequest();
    UUID id = UUID.randomUUID();
    MenuCategory menuCategory = TestFactory.menuCategory();
    CategoryResponse categoryResponse = TestFactory.categoryResponse(menuCategory);
    when(categoryMapper.toEntity(request, id)).thenReturn(menuCategory);
    when(categoryRepository.findTopByOrderByPositionDesc()).thenReturn(Optional.empty());
    when(categoryRepository.save(menuCategory)).thenReturn(menuCategory);
    when(categoryMapper.toResponse(menuCategory)).thenReturn(categoryResponse);

    CategoryResponse result = categoryService.create(request, id);

    assertThat(result).isEqualTo(categoryResponse);
    assertThat(menuCategory.getPosition()).isEqualTo(0);
  }

  @Test
  void create_setsNextPositionAfterExisting() {
    CategoryRequest request = TestFactory.categoryRequest();
    UUID id = UUID.randomUUID();
    MenuCategory existing = TestFactory.menuCategory();
    existing.setPosition(2);
    MenuCategory newCategory = TestFactory.menuCategory();
    CategoryResponse categoryResponse = TestFactory.categoryResponse(newCategory);

    when(categoryMapper.toEntity(request, id)).thenReturn(newCategory);
    when(categoryRepository.findTopByOrderByPositionDesc()).thenReturn(Optional.of(existing));
    when(categoryRepository.save(newCategory)).thenReturn(newCategory);
    when(categoryMapper.toResponse(newCategory)).thenReturn(categoryResponse);

    categoryService.create(request, id);

    assertThat(newCategory.getPosition()).isEqualTo(3);
  }

  @Test
  void update_updatesAndReturnsCategory() {
    UUID userId = UUID.randomUUID();
    MenuCategory menuCategory = TestFactory.menuCategory();
    CategoryRequest request = TestFactory.categoryRequest();
    CategoryResponse categoryResponse = TestFactory.categoryResponse(TestFactory.menuCategory());

    when(categoryRepository.findById(any())).thenReturn(Optional.of(menuCategory));
    when(categoryMapper.toResponse(menuCategory)).thenReturn(categoryResponse);
    CategoryResponse result = categoryService.update(userId, request, userId);

    assertThat(result).isEqualTo(categoryResponse);
    verify(categoryMapper).updateEntity(request, userId, menuCategory);
  }

  @Test
  void update_throwsWhenNotFound() {
    UUID categoryId = UUID.randomUUID();
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> categoryService.update(categoryId, TestFactory.categoryRequest(), UUID.randomUUID()))
      .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void delete_deletesCategory() {
    UUID categoryId = UUID.randomUUID();
    when(categoryRepository.existsById(categoryId)).thenReturn(true);
    categoryService.delete(categoryId);
    verify(categoryRepository).deleteById(categoryId);
  }

  @Test
  void delete_throwsWhenNotFound() {
    UUID categoryId = UUID.randomUUID();
    when(categoryRepository.existsById(categoryId)).thenReturn(false);
    assertThatThrownBy(() -> categoryService.delete(categoryId)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updatePositions_updatesPositionOnEachCategory() {
    MenuCategory cat1 = TestFactory.menuCategory();
    MenuCategory cat2 = TestFactory.menuCategory();

    List<PositionEntry> entries = List.of(
      new PositionEntry(cat1.getId(), 1),
      new PositionEntry(cat2.getId(), 0)
    );

    when(categoryRepository.findAllById(any())).thenReturn(List.of(cat1, cat2));

    categoryService.updatePositions(entries);

    assertThat(cat1.getPosition()).isEqualTo(1);
    assertThat(cat2.getPosition()).isEqualTo(0);
    verify(categoryRepository).saveAll(any());
  }
}
