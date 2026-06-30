package com.example.orders.order.dto;

import com.example.orders.order.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Filter parameters for admin order search")
public record OrderFilterRequest(

  @Schema(description = "Filter by one or more statuses", example = "[\"PLACED\", \"CONFIRMED\"]")
  List<OrderStatus> statuses,

  @Schema(description = "Partial match on customer email (case-insensitive)", example = "john")
  String userEmailSearch,

  @Schema(description = "Orders placed on or after this date", example = "2026-06-01")
  LocalDate dateFrom,

  @Schema(description = "Orders placed before or on this date. Defaults to today if dateFrom is set and dateTill is not", example = "2026-06-30")
  LocalDate dateTill,

  @Schema(description = "Field to sort by", example = "CREATED_AT")
  SortBy sortBy,

  @Schema(description = "Sort direction", example = "DESC")
  SortDir sortDir
) {
  public enum SortBy {CREATED_AT, LAST_MODIFIED_AT, TOTAL_PRICE}

  public enum SortDir {ASC, DESC}
}
