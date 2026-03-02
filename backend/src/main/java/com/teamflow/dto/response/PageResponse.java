package com.teamflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * Why not return Spring's Page<T> directly from controllers?
 * - Page<T> has many internal fields that clutter the API response
 * - Our PageResponse exposes only what the frontend actually needs
 * - Decouples API contract from Spring internals
 *
 * Usage: PageResponse.from(page) — converts any Spring Page to our format.
 *
 * @param <T> the type of items in the page (e.g. ProjectResponse, TaskResponse)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /**
     * Static factory method: converts Spring's Page<T> into our PageResponse<T>.
     * Called in mappers or service layer.
     *
     * Example usage:
     *   Page<Project> projects = projectRepository.findByOwnerId(id, pageable);
     *   Page<ProjectResponse> dtos = projects.map(projectMapper::toResponse);
     *   return PageResponse.from(dtos);
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
