package com.medagenda.common.web;

import java.text.Normalizer;
import java.util.Locale;

/** Helpers to turn optional free-text filters into SQL LIKE patterns. */
public final class SearchTerms {

    private SearchTerms() {
    }

    /**
     * Returns a lower-case, accent-free "contains" pattern, or "%" (match everything) when the
     * term is blank. The queries compare it with {@code lower(function('unaccent', column))},
     * so "fabio" finds "Fábio". Building the pattern in Java also keeps the JPQL free of
     * "(:param is null or ...)" tricks, which PostgreSQL cannot type when the parameter is null.
     */
    public static String contains(String term) {
        if (term == null || term.isBlank()) {
            return "%";
        }
        return "%" + stripAccents(term.strip()).toLowerCase(Locale.ROOT) + "%";
    }

    /** "Fábio" -> "Fabio": decompose letters from their accents, then drop the accent marks. */
    static String stripAccents(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }
}
