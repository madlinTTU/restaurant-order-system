package com.example.orders.storage;

import com.example.orders.menu.model.MenuItem;
import com.example.orders.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCleanupJob {

    private final MenuItemRepository itemRepository;
    private final StorageService storageService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOrphanedImages() {
        List<MenuItem> items = itemRepository.findAllByImageUrlIsNotNull();
        int cleaned = 0;

        for (MenuItem item : items) {
            String key = storageService.extractKey(item.getImageUrl());
            if (!storageService.objectExists(key)) {
                log.info("Removing orphaned image_url for menu item {}", item.getId());
                item.setImageUrl(null);
                cleaned++;
            }
        }

        if (cleaned > 0) {
            log.info("Image cleanup complete: {} orphaned records cleared", cleaned);
        }
    }
}
