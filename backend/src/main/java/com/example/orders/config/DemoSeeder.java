package com.example.orders.config;

import com.example.orders.auth.model.Role;
import com.example.orders.auth.model.User;
import com.example.orders.auth.repository.UserRepository;
import com.example.orders.menu.model.MenuCategory;
import com.example.orders.menu.model.MenuItem;
import com.example.orders.menu.repository.MenuCategoryRepository;
import com.example.orders.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;

        UUID adminId = seedUsers();
        seedMenu(adminId);
    }

    private UUID seedUsers() {
        User admin = user("admin@demo.com", "admin123", Role.ADMIN);
        userRepository.save(admin);
        userRepository.saveAll(List.of(
                user("kitchen@demo.com", "kitchen123", Role.KITCHEN),
                user("customer@demo.com", "customer123", Role.CUSTOMER)
        ));
        return admin.getId();
    }

    private void seedMenu(UUID adminId) {
        MenuCategory pizza   = category("Pizza",   "Classic Italian pizzas",      1, adminId);
        MenuCategory pasta   = category("Pasta",   "Freshly made pasta dishes",   2, adminId);
        MenuCategory salads  = category("Salads",  "Fresh seasonal salads",       3, adminId);
        MenuCategory drinks  = category("Drinks",  "Cold and hot beverages",      4, adminId);
        menuCategoryRepository.saveAll(List.of(pizza, pasta, salads, drinks));

        menuItemRepository.saveAll(List.of(
                item(pizza,  "Margherita",          "Tomato sauce, mozzarella, basil",               8.99,  1, adminId),
                item(pizza,  "Pepperoni",            "Tomato sauce, mozzarella, pepperoni",           10.99, 2, adminId),
                item(pizza,  "Quattro Formaggi",     "Four cheese blend on white sauce",              11.99, 3, adminId),
                item(pasta,  "Carbonara",            "Guanciale, egg yolk, pecorino, black pepper",    8.49, 1, adminId),
                item(pasta,  "Bolognese",            "Slow-cooked beef ragu, tagliatelle",             7.99, 2, adminId),
                item(pasta,  "Fettuccine Alfredo",   "Butter, parmesan, fettuccine",                   8.99, 3, adminId),
                item(salads, "Caesar",               "Romaine, grilled chicken, croutons, parmesan",   6.49, 1, adminId),
                item(salads, "Greek",                "Tomato, cucumber, olives, feta",                 5.49, 2, adminId),
                item(drinks, "Lemonade",             "Fresh squeezed, mint",                           2.49, 1, adminId),
                item(drinks, "Orange Juice",         "Freshly squeezed",                               1.99, 2, adminId),
                item(drinks, "Espresso",             "Double shot",                                    1.99, 3, adminId)
        ));
    }

    private User user(String email, String password, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        return u;
    }

    private MenuCategory category(String name, String description, int position, UUID adminId) {
        MenuCategory c = new MenuCategory();
        c.setName(name);
        c.setDescription(description);
        c.setPosition(position);
        c.setCreatedBy(adminId);
        c.setModifiedBy(adminId);
        return c;
    }

    private MenuItem item(MenuCategory category, String name, String description, double price, int position, UUID adminId) {
        MenuItem i = new MenuItem();
        i.setCategory(category);
        i.setName(name);
        i.setDescription(description);
        i.setPrice(BigDecimal.valueOf(price));
        i.setPosition(position);
        i.setAvailable(true);
        i.setCreatedBy(adminId);
        i.setModifiedBy(adminId);
        return i;
    }
}
