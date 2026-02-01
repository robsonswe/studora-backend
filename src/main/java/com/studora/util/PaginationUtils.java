package com.studora.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for pagination and sorting operations.
 */
public class PaginationUtils {

    /**
     * Applies a priority-based sort to a Pageable object.
     *
     * @param pageable         The original Pageable object (containing page and size).
     * @param primarySort      The primary field to sort by.
     * @param primaryDirection The direction of the primary sort (ASC/DESC).
     * @param propertyMapping  A map to translate API sort names to entity property paths.
     * @param tieBreakers      A list of default Sort.Order objects to use as tie-breakers.
     * @return A new Pageable object with the applied priority sorting.
     */
    public static Pageable applyPrioritySort(
            Pageable pageable,
            String primarySort,
            String primaryDirection,
            Map<String, String> propertyMapping,
            List<Sort.Order> tieBreakers) {

        Sort.Direction dir = Sort.Direction.fromString(primaryDirection.toUpperCase());
        String sortProperty = propertyMapping.getOrDefault(primarySort, primarySort);

        List<Sort.Order> orders = new ArrayList<>();
        
        // 1. Add primary sort chosen by user
        orders.add(new Sort.Order(dir, sortProperty));

        // 2. Add tie-breakers, skipping the user-selected property if it's already there
        for (Sort.Order tieBreaker : tieBreakers) {
            if (!tieBreaker.getProperty().equalsIgnoreCase(sortProperty)) {
                orders.add(tieBreaker);
            }
        }

        // 3. Ensure final deterministic tie-breaker: ID descending
        boolean hasIdSort = orders.stream()
                .anyMatch(order -> order.getProperty().equalsIgnoreCase("id"));
        if (!hasIdSort) {
            orders.add(Sort.Order.desc("id"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }
}
