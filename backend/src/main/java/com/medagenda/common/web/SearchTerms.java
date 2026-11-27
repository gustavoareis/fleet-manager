package com.medagenda.common.web;

/** Helpers to turn optional free-text filters into SQL LIKE patterns. */
public final class SearchTerms {

    private SearchTerms() {
    }

    /**
     * Returns a lower-case "contains" pattern, or "%" (match everything) when the term is blank.
     * Building the pattern in Java keeps the JPQL free of "(:param is null or ...)" tricks,
     * which PostgreSQL cannot type when the parameter is null.
     */
    public static String contains(String term) {
        if (term == null || term.isBlank()) {
            return "%";
        }
        return "%" + term.strip().toLowerCase() + "%";
    }
}
