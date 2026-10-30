package com.ppossatto.tensio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SearchPatternsTest {

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void blankMatchesEverything(String text) {
    assertThat(SearchPatterns.contains(text)).isEqualTo("%");
  }

  @Test
  void trimsAndLowerCases() {
    assertThat(SearchPatterns.contains("  TR-01 ")).isEqualTo("%tr-01%");
  }

  @Test
  void escapesWildcardsAndEscapeCharacter() {
    assertThat(SearchPatterns.contains("a%b_c!d")).isEqualTo("%a!%b!_c!!d%");
  }
}
