package com.example.orders.order.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

  @Id
  @Column(name = "uuid")
  private UUID id;

  @Column(name = "user_uuid", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus orderStatus;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  List<OrderItem> items = new ArrayList<>();

  @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalPrice;

  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "created_by", nullable = false, updatable = false)
  private UUID createdBy;

  @Column(name = "modified_at", nullable = false)
  private OffsetDateTime modifiedAt;

  @Column(name = "modified_by", nullable = false)
  private UUID modifiedBy;

  @Version
  @Column(nullable = false)
  private Long version;

  @PrePersist
  public void prePersist() {
    if (id == null) id = UUID.randomUUID();
    if (orderStatus == null) orderStatus = OrderStatus.PLACED;
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (modifiedAt == null) modifiedAt = OffsetDateTime.now();
    if (version == null) version = 0L;
  }

  @PreUpdate
  public void preUpdate() {
    modifiedAt = OffsetDateTime.now();
  }
}
