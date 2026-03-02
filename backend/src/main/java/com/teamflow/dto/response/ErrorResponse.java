package com.teamflow.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified error response format for all API errors.
 *
 * @JsonInclude(NON_NULL): fields that are null are excluded from JSON.
 * 'errors' field is only present for validation errors (400),
 * not for 404/403/500 responses.
 *
 * Example 404 response:
 * {
 *   "status": 404,
 *   "message": "Project not found with id: 1",
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 *
 * Example 400 validation response:
 * {
 *   "status": 400,
 *   "message": "Validation failed",
 *   "timestamp": "...",
 *   "errors": {
 *     "email": "Email must be valid",
 *     "name": "Name is required"
 *   }
 * }
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors; // field-level validation errors
}
