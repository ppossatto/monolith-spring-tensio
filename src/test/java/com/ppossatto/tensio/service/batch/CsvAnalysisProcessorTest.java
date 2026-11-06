package com.ppossatto.tensio.service.batch;

import com.ppossatto.tensio.domain.transformer.TransformerStatus;
import com.ppossatto.tensio.entity.TransformerEntity;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CsvAnalysisProcessorTest {

  private static final ValidatorFactory VALIDATION = Validation.buildDefaultValidatorFactory();
  private static final Validator VALIDATOR = VALIDATION.getValidator();
  private static final String HEADER = "transformer_tag,sample_date,h2,ch4,c2h2,c2h4,c2h6,co,co2,o2,n2";

  @Mock
  private TransformerRepository transformers;
  @Mock
  private AnalysisRepository analyses;

  private CsvAnalysisProcessor processor;

  @AfterAll
  static void closeValidation() {
    VALIDATION.close();
  }

  @BeforeEach
  void createProcessor() {
    var operating = transformer(1L, TransformerStatus.OPERATION);
    var decommissioned = transformer(12L, TransformerStatus.DECOMMISSIONED);
    when(transformers.findByTagIgnoreCase(anyString())).thenReturn(Optional.empty());
    when(transformers.findByTagIgnoreCase("TT-01")).thenReturn(Optional.of(operating));
    when(transformers.findByTagIgnoreCase("TT-12")).thenReturn(Optional.of(decommissioned));
    processor = new CsvAnalysisProcessor(transformers, analyses, VALIDATOR);
    processor.header(HEADER);
  }

  @Test
  void convertsAValidLineIntoAForm() {
    var result = processor.process(new CsvLine(2, "tt-01,2026-01-10,600,120,200,250,30,300,2500,3100.5,45200"));

    assertThat(result.line()).isEqualTo(2);
    var form = result.form();
    assertThat(form.getTransformerId()).isEqualTo(1L);
    assertThat(form.getSampleDate()).isEqualTo(LocalDate.of(2026, 1, 10));
    assertThat(form.getH2()).isEqualByComparingTo("600");
    assertThat(form.getCh4()).isEqualByComparingTo("120");
    assertThat(form.getC2h2()).isEqualByComparingTo("200");
    assertThat(form.getC2h4()).isEqualByComparingTo("250");
    assertThat(form.getC2h6()).isEqualByComparingTo("30");
    assertThat(form.getCo()).isEqualByComparingTo("300");
    assertThat(form.getCo2()).isEqualByComparingTo("2500");
    assertThat(form.getO2()).isEqualByComparingTo("3100.5");
    assertThat(form.getN2()).isEqualByComparingTo("45200");
  }

  @Test
  void optionalGasesMayBeEmpty() {
    var form = processor.process(new CsvLine(2, "TT-01,2026-01-10,20,10,0,5,8,200,1800,,")).form();

    assertThat(form.getO2()).isNull();
    assertThat(form.getN2()).isNull();
  }

  @Test
  void blankLineIsFiltered() {
    assertThat(processor.process(new CsvLine(5, "   "))).isNull();
  }

  @Test
  void unknownTransformerIsRejected() {
    assertRejected("TT-99,2026-01-10,20,10,0,5,8,200,1800",
       "Line 3: transformer TT-99 does not exist or is decommissioned");
  }

  @Test
  void decommissionedTransformerIsRejected() {
    assertRejected("TT-12,2026-01-10,20,10,0,5,8,200,1800",
       "Line 3: transformer TT-12 does not exist or is decommissioned");
  }

  @Test
  void emptyTagIsRejected() {
    assertRejected(",2026-01-10,20,10,0,5,8,200,1800", "Line 3: transformer_tag is empty");
  }

  @Test
  void invalidNumberIsRejected() {
    assertRejected("TT-01,2026-01-10,x,10,0,5,8,200,1800", "Line 3: h2 is not a number: 'x'");
  }

  @Test
  void invalidDateIsRejected() {
    assertRejected("TT-01,2026/01/10,20,10,0,5,8,200,1800",
       "Line 3: sample_date is not a date (yyyy-MM-dd or dd/MM/yyyy): '2026/01/10'");
  }

  @Test
  void beanValidationErrorsAreListedByProperty() {
    // Messages depend on the JVM locale: only the property names and their order are checked
    assertThatThrownBy(() -> processor.process(new CsvLine(3, "TT-01,2026-01-10,-1,10,,5,8,200,1800")))
       .isInstanceOf(CsvLineException.class)
       .hasMessageStartingWith("Line 3: c2h2 ")
       .hasMessageContaining(", h2 ");
  }

  @Test
  void secondLineForTheSameTransformerAndDateIsRejected() {
    processor.process(new CsvLine(2, "TT-01,2026-01-10,20,10,0,5,8,200,1800"));

    assertRejected("tt-01,10/01/2026,30,10,0,5,8,200,1800",
       "Line 3: the transformer already has an analysis on 2026-01-10");
  }

  @Test
  void dateAlreadyInTheDatabaseIsRejected() {
    when(analyses.existsByTransformerIdAndSampleDate(1L, LocalDate.of(2026, 1, 10))).thenReturn(true);

    assertRejected("TT-01,2026-01-10,20,10,0,5,8,200,1800",
       "Line 3: the transformer already has an analysis on 2026-01-10");
  }

  @Test
  void knownTransformerIsLookedUpOnce() {
    processor.process(new CsvLine(2, "TT-01,2026-01-10,20,10,0,5,8,200,1800"));
    processor.process(new CsvLine(3, "tt-01,2026-01-11,20,10,0,5,8,200,1800"));

    verify(transformers, times(1)).findByTagIgnoreCase("TT-01");
  }

  @Test
  void headerFromTheFileDecidesTheColumns() {
    processor.header("co2;co;c2h6;c2h4;c2h2;ch4;h2;sample_date;transformer_tag");

    var form = processor.process(new CsvLine(2, "2500;300;30;250;200;120;600,5;10/01/2026;TT-01")).form();

    assertThat(form.getH2()).isEqualByComparingTo("600.5");
    assertThat(form.getCo2()).isEqualByComparingTo("2500");
    assertThat(form.getSampleDate()).isEqualTo(LocalDate.of(2026, 1, 10));
  }

  private void assertRejected(String content, String message) {
    assertThatThrownBy(() -> processor.process(new CsvLine(3, content)))
       .isInstanceOf(CsvLineException.class)
       .hasMessage(message);
  }

  private static TransformerEntity transformer(long id, TransformerStatus status) {
    var entity = mock(TransformerEntity.class);
    when(entity.getId()).thenReturn(id);
    when(entity.getStatus()).thenReturn(status);
    return entity;
  }
}
