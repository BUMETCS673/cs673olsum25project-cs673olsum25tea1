package com.bu.getactivecore.shared;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PaginatedResponse<T> {

	List<T> content;
	int page;
	int size;
	long totalElements;
	int totalPages;
	boolean first;
	boolean last;

	String nextPageUrl;
	String previousPageUrl;

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

	private String buildPageUrl(String basePath, int page, int size, Map<String, String> params) {
		StringBuilder url = new StringBuilder(basePath + "?page=" + page + "&size=" + size);
		params.forEach((k, v) -> url.append("&").append(k).append("=").append(v));
		return url.toString();
	}
}
