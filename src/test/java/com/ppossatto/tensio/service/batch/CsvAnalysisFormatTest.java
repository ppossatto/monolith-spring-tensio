package com.ppossatto.tensio.service.batch;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvAnalysisFormatTest {

  private static final String HEADER = "transformer_tag,sample_date,h2,ch4,c2h2,c2h4,c2h6,co,co2";

  @Test
  void headerMapsColumnsByName() {
    var header = CsvAnalysisFormat.header(HEADER + ",o2,n2");

    assertThat(header.separator()).isEqualTo(",");
    assertThat(header.columns()).containsEntry("transformer_tag", 0).containsEntry("co2", 8).containsEntry("n2", 10);
  }

  @Test
  void headerIgnoresCaseSpacesAndByteOrderMark() {
    var header = CsvAnalysisFormat.header("﻿ Transformer_Tag ; SAMPLE_DATE;h2;ch4;c2h2;c2h4;c2h6;co;co2 ");

    assertThat(header.separator()).isEqualTo(";");
    assertThat(header.columns()).containsEntry("transformer_tag", 0).containsEntry("sample_date", 1);
  }

  @Test
  void headerKeepsTheFirstOfRepeatedColumns() {
    assertThat(CsvAnalysisFormat.header(HEADER + ",h2").columns()).containsEntry("h2", 2);
  }

  @Test
  void headerListsEveryMissingColumn() {
    assertThatThrownBy(() -> CsvAnalysisFormat.header("transformer_tag,h2,ch4,c2h2,c2h4,c2h6,co"))
       .isInstanceOf(IllegalArgumentException.class)
       .hasMessage("The header is missing the columns: sample_date, co2");
  }

  @Test
  void nullHeaderIsMissingEveryColumn() {
    assertThatThrownBy(() -> CsvAnalysisFormat.header(null))
       .hasMessageStartingWith("The header is missing the columns: transformer_tag, sample_date");
  }

  @Test
  void cellsAreTrimmedAndUnquoted() {
    var header = CsvAnalysisFormat.header(HEADER);

    assertThat(CsvAnalysisFormat.cells(header, " TT-01 ,\" 2026-01-10 \",\"\",x"))
       .containsExactly("TT-01", "2026-01-10", "", "x");
  }

  @Test
  void textIsNullWhenEmptyOrMissing() {
    var header = CsvAnalysisFormat.header(HEADER + ",o2");
    var cells = CsvAnalysisFormat.cells(header, "TT-01,2026-01-10,,1");

    assertThat(CsvAnalysisFormat.text(header, cells, "transformer_tag")).isEqualTo("TT-01");
    assertThat(CsvAnalysisFormat.text(header, cells, "h2")).isNull();
    assertThat(CsvAnalysisFormat.text(header, cells, "o2")).isNull();
    assertThat(CsvAnalysisFormat.text(header, cells, "n2")).isNull();
  }

  @Test
  void numbersUseDotWithCommaSeparator() {
    var header = CsvAnalysisFormat.header(HEADER);
    var cells = CsvAnalysisFormat.cells(header, "TT-01,2026-01-10,12.5");

    assertThat(CsvAnalysisFormat.number(header, cells, "h2")).isEqualByComparingTo(new BigDecimal("12.5"));
    assertThat(CsvAnalysisFormat.number(header, cells, "ch4")).isNull();
  }

  @Test
  void numbersAcceptDecimalCommaWithSemicolonSeparator() {
    var header = CsvAnalysisFormat.header(HEADER.replace(',', ';'));
    var cells = CsvAnalysisFormat.cells(header, "TT-01;2026-01-10;12,5;7.25");

    assertThat(CsvAnalysisFormat.number(header, cells, "h2")).isEqualByComparingTo("12.5");
    assertThat(CsvAnalysisFormat.number(header, cells, "ch4")).isEqualByComparingTo("7.25");
  }

  @Test
  void invalidNumberNamesTheColumn() {
    var header = CsvAnalysisFormat.header(HEADER);
    var cells = CsvAnalysisFormat.cells(header, "TT-01,2026-01-10,12;5");

    assertThatThrownBy(() -> CsvAnalysisFormat.number(header, cells, "h2"))
       .isInstanceOf(IllegalArgumentException.class)
       .hasMessage("h2 is not a number: '12;5'");
  }

  @Test
  void datesAcceptIsoAndDayFirst() {
    var header = CsvAnalysisFormat.header(HEADER);

    assertThat(CsvAnalysisFormat.date(header, CsvAnalysisFormat.cells(header, "TT-01,2026-01-10"), "sample_date"))
       .isEqualTo(LocalDate.of(2026, 1, 10));
    assertThat(CsvAnalysisFormat.date(header, CsvAnalysisFormat.cells(header, "TT-01,10/01/2026"), "sample_date"))
       .isEqualTo(LocalDate.of(2026, 1, 10));
    assertThat(CsvAnalysisFormat.date(header, CsvAnalysisFormat.cells(header, "TT-01,"), "sample_date")).isNull();
  }

  @Test
  void invalidDateNamesTheAcceptedFormats() {
    var header = CsvAnalysisFormat.header(HEADER);
    var cells = CsvAnalysisFormat.cells(header, "TT-01,10-01-2026");

    assertThatThrownBy(() -> CsvAnalysisFormat.date(header, cells, "sample_date"))
       .isInstanceOf(IllegalArgumentException.class)
       .hasMessage("sample_date is not a date (yyyy-MM-dd or dd/MM/yyyy): '10-01-2026'");
  }
}
