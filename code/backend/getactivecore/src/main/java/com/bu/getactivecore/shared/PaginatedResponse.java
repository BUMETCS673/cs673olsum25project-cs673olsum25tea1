package com.bu.getactivecore.shared;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * A generic data transfer object representing a paginated response for REST
 * APIs.
 * <p>
 * This class encapsulates the core details of a paginated dataset including: -
 * The content for the current page - Pagination metadata (current page number,
 * page size, total items, etc.) - Navigation URLs to next and previous pages
 * </p>
 *
 * @param <T> the type of data contained in the page content
 */
@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PaginatedResponse<T> {

	/**
	 * The content items of the current page.
	 */
	List<T> content;

	/**
	 * The current page number (zero-based index).
	 */
	int page;

	/**
	 * The number of items per page.
	 */
	int size;

	/**
	 * The total number of items across all pages.
	 */
	long totalElements;

	/**
	 * The total number of pages.
	 */
	int totalPages;

	/**
	 * Indicates whether this is the first page.
	 */
	boolean first;

	/**
	 * Indicates whether this is the last page.
	 */
	boolean last;

	/**
	 * The URL for the next page of results (null if this is the last page).
	 */
	String nextPageUrl;

	/**
	 * The URL for the previous page of results (null if this is the first page).
	 */
	String previousPageUrl;

	/**
	 * Constructs a {@link PaginatedResponse} from a Spring {@link Page} object and
	 * generates navigation URLs using the provided base path and additional query
	 * parameters.
	 *
	 * @param pageData         the page data from Spring Data
	 * @param basePath         the base path for building pagination links (e.g:
	 *                         "/v1/activities")
	 * @param additionalParams any additional query parameters to preserve in
	 *                         pagination links
	 */
	public PaginatedResponse(Page<T> pageData, String basePath, Map<String, String> additionalParams) {
		content = pageData.getContent();
		page = pageData.getNumber();
		size = pageData.getSize();
		totalElements = pageData.getTotalElements();
		totalPages = pageData.getTotalPages();
		first = pageData.isFirst();
		last = pageData.isLast();

		nextPageUrl = !pageData.isLast() ? buildPageUrl(basePath, page + 1, size, additionalParams) : null;
		previousPageUrl = !pageData.isFirst() ? buildPageUrl(basePath, page - 1, size, additionalParams) : null;
	}

	/**
	 * Builds a full URL for a specific page number using a base path and query
	 * parameters.
	 *
	 * @param basePath the base URI path (e.g., "/api/items")
	 * @param page     the page number to include in the URL
	 * @param size     the page size to include in the URL
	 * @param params   additional query parameters to include
	 * @return the constructed pagination URL
	 */
	private String buildPageUrl(String basePath, int page, int size, Map<String, String> params) {
		StringBuilder url = new StringBuilder(basePath + "?page=" + page + "&size=" + size);
		params.forEach((k, v) -> url.append("&").append(k).append("=").append(v));
		return url.toString();
	}
}
