package com.poc.hub.core.feature;

/**
 * Contract every feature module implements (one {@code @Component} per feature).
 * {@link FeatureController} discovers all implementations, so adding a feature never
 * requires editing a shared list - this keeps later phases purely additive for Git merges.
 */
public interface FeatureDescriptor {

    /** Stable key, also the REST namespace, e.g. "tasks". */
    String key();

    /** Display name, e.g. "Tasks". */
    String name();

    /** One line shown on the home card. */
    String description();

    /** UI route, e.g. "/tasks". */
    String route();

    /** Primary list endpoint, e.g. "/api/tasks". GET must answer 200 with JSON. */
    String apiPath();

    /** Phase in which the feature was introduced (1, 2 or 3). */
    int phase();

    /** Sort order on the home page and in the navigation. */
    int order();

    /** Live number of rows of the feature's main table. */
    long itemCount();
}
