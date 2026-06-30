package com.example.orders.auth.repository;

import com.example.orders.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Set<UUID> findIdsByEmailContainingIgnoreCase(@Param("search") String search);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIdIn(@Param("ids") Set<UUID> ids);
}
