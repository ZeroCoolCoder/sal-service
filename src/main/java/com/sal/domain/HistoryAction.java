package com.sal.domain;

/**
 * Actions recorded in history.
 */
public enum HistoryAction {
    CREATE("Version created"),
    UPDATE("Metadata updated"),
    DELETE("Version deleted"),
    STATUS_CHANGE("Status changed"),
    SET_LATEST("Set as latest version");

    private final String description;

    HistoryAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
