package com.teamflow.entity;

/**
 * Task lifecycle status.
 * Stored as STRING in DB (not ordinal) for readability and safety.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
