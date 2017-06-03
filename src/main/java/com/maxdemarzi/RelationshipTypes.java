package com.maxdemarzi;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    IN_AISLE,
    IN_DEPARTMENT,
    ORDERED_BY,
    PREV,
    HAS
}