package model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginatedResponse<T> {
	private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalItems;

}
