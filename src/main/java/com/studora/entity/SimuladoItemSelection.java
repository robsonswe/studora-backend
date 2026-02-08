package com.studora.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimuladoItemSelection {
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Column(nullable = false)
    private int quantidade;
}
