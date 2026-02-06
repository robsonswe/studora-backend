package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.studora.common.constants.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estrutura simplificada para respostas paginadas")
public class PageResponse<T> {

    @Schema(description = "Lista de elementos na página atual")
    private List<T> content;

    @Schema(description = "Número da página atual (0..N)", example = AppConstants.DEFAULT_PAGE_NUMBER_STR)
    private int pageNumber;
    
    @Schema(description = "Tamanho da página", example = AppConstants.DEFAULT_PAGE_SIZE_STR)
    private int pageSize;

    @Schema(description = "Total de elementos em todas as páginas", example = "100")
    private long totalElements;

    @Schema(description = "Total de páginas disponíveis", example = "5")
    private int totalPages;

    @Schema(description = "Indica se esta é a última página", example = "false")
    private boolean last;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }
}
