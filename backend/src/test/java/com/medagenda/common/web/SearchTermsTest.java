package com.medagenda.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SearchTermsTest {

    @Test
    void blankOrNullMatchesEverything() {
        assertThat(SearchTerms.contains(null)).isEqualTo("%");
        assertThat(SearchTerms.contains("   ")).isEqualTo("%");
    }

    @Test
    void removesAccentsLowercasesAndWrapsInWildcards() {
        assertThat(SearchTerms.contains("  FÁBIO ")).isEqualTo("%fabio%");
        assertThat(SearchTerms.contains("Obstetrícia")).isEqualTo("%obstetricia%");
    }

    @Test
    void stripAccentsKeepsPlainLettersUntouched() {
        assertThat(SearchTerms.stripAccents("coração João")).isEqualTo("coracao Joao");
    }
}
