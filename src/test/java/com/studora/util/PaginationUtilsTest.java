package com.studora.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

class PaginationUtilsTest {

    @Test
    void applyPrioritySort_primarySortOnly_addsIdTieBreaker() {
        Pageable input = PageRequest.of(0, 10);
        Pageable result = PaginationUtils.applyPrioritySort(input, "nome", "ASC", Map.of(), List.of());

        Sort sort = result.getSort();
        List<Sort.Order> orders = sort.toList();

        assertEquals(2, orders.size());
        assertEquals("nome", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("id", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
    }

    @Test
    void applyPrioritySort_mappedProperty_translatesName() {
        Pageable input = PageRequest.of(0, 20);
        Map<String, String> mapping = Map.of("temaId", "tema.id");

        Pageable result = PaginationUtils.applyPrioritySort(input, "temaId", "DESC", mapping, List.of());

        Sort.Order primaryOrder = result.getSort().toList().get(0);
        assertEquals("tema.id", primaryOrder.getProperty());
        assertEquals(Sort.Direction.DESC, primaryOrder.getDirection());
    }

    @Test
    void applyPrioritySort_unmappedProperty_passesThrough() {
        Pageable input = PageRequest.of(2, 5);
        Pageable result = PaginationUtils.applyPrioritySort(input, "createdAt", "ASC", Map.of(), List.of());

        Sort.Order primaryOrder = result.getSort().toList().get(0);
        assertEquals("createdAt", primaryOrder.getProperty());
    }

    @Test
    void applyPrioritySort_preservesPageAndSize() {
        Pageable input = PageRequest.of(3, 15);
        Pageable result = PaginationUtils.applyPrioritySort(input, "nome", "ASC", Map.of(), List.of());

        assertEquals(3, result.getPageNumber());
        assertEquals(15, result.getPageSize());
    }

    @Test
    void applyPrioritySort_tieBreakerSkippedIfMatchesPrimarySort() {
        Pageable input = PageRequest.of(0, 10);
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),  // same property as primary
            Sort.Order.asc("tema.id")
        );

        Pageable result = PaginationUtils.applyPrioritySort(input, "nome", "ASC", Map.of(), tieBreakers);

        List<Sort.Order> orders = result.getSort().toList();
        // nome (primary), tema.id (tie-breaker), id (final tie-breaker)
        assertEquals(3, orders.size());
        assertEquals("nome", orders.get(0).getProperty());
        assertEquals("tema.id", orders.get(1).getProperty());
        assertEquals("id", orders.get(2).getProperty());
    }

    @Test
    void applyPrioritySort_tieBreakersAdded() {
        Pageable input = PageRequest.of(0, 10);
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("tema.id")
        );

        Pageable result = PaginationUtils.applyPrioritySort(input, "createdAt", "DESC", Map.of(), tieBreakers);

        List<Sort.Order> orders = result.getSort().toList();
        assertEquals("createdAt", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());
        assertEquals("nome", orders.get(1).getProperty());
        assertEquals("tema.id", orders.get(2).getProperty());
        assertEquals("id", orders.get(3).getProperty());
    }

    @Test
    void applyPrioritySort_idIsPrimary_noExtraIdAdded() {
        Pageable input = PageRequest.of(0, 10);
        Pageable result = PaginationUtils.applyPrioritySort(input, "id", "ASC", Map.of(), List.of());

        List<Sort.Order> orders = result.getSort().toList();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getProperty());
    }

    @Test
    void applyPrioritySort_caseInsensitiveDirection() {
        Pageable input = PageRequest.of(0, 10);
        Pageable result = PaginationUtils.applyPrioritySort(input, "nome", "desc", Map.of(), List.of());

        assertEquals(Sort.Direction.DESC, result.getSort().toList().get(0).getDirection());
    }
}
