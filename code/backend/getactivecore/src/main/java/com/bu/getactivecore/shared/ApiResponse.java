package com.bu.getactivecore.shared;

/**
 * Represents a generic API response.
 *
 * @param data the data returned from the API
 * @param <T>  the type of the data
 */
public record ApiResponse<T>(T data) {
}
