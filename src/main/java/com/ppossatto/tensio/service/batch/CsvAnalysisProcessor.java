package com.ppossatto.tensio.service.batch;

import com.ppossatto.tensio.entity.TransformerEntity;
import com.ppossatto.tensio.form.AnalysisForm;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import jakarta.validation.Validator;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ppossatto.tensio.domain.transformer.TransformerStatus.DECOMMISSIONED;

/// Converts a CSV line into a validated analysis input, or throws {@link CsvLineException} so the line is
/// skipped and reported. Every check that could make the store fail is done here (unknown or decommissioned
/// transformer, existing analysis on the date, the same transformer and date twice in the file), so the
/// writer only stores valid analyses. One instance per import; not thread-safe.
class CsvAnalysisProcessor implements ItemProcessor<CsvLine, CsvAnalysis> {

  private final TransformerRepository transformers;
  private final AnalysisRepository analyses;
  private final Validator validator;

  /// Transformer id by upper-case tag, `null` when unknown or decommissioned.
  private final Map<String, Long> transformerIds = new HashMap<>();
  /// "transformerId|date" already accepted in this file.
  private final Set<String> accepted = new HashSet<>();
  private CsvAnalysisFormat.Header header;

  CsvAnalysisProcessor(TransformerRepository transformers, AnalysisRepository analyses, Validator validator) {
    this.transformers = transformers;
    this.analyses = analyses;
    this.validator = validator;
  }

  /// Receives the header line skipped by the reader.
  ///
  /// @param line the first line of the file
  void header(String line) {
    header = CsvAnalysisFormat.header(line);
  }

  @Override
  public CsvAnalysis process(CsvLine line) {
    if (line.content().isBlank()) {
      return null; // filtered: blank lines are ignored
    }
    try {
      var cells = CsvAnalysisFormat.cells(header, line.content());
      var form = form(line, cells);

      var violations = validator.validate(form);
      if (!violations.isEmpty()) {
        throw new CsvLineException(line.number(), violations.stream()
           .map(v -> v.getPropertyPath() + " " + v.getMessage())
           .sorted()
           .collect(Collectors.joining(", ")));
      }
      if (!accepted.add(form.getTransformerId() + "|" + form.getSampleDate())
         || analyses.existsByTransformerIdAndSampleDate(form.getTransformerId(), form.getSampleDate())) {
        throw new CsvLineException(line.number(),
           "the transformer already has an analysis on " + form.getSampleDate());
      }
      return new CsvAnalysis(line.number(), form);
    } catch (IllegalArgumentException e) {
      throw new CsvLineException(line.number(), e.getMessage());
    }
  }

  private AnalysisForm form(CsvLine line, String[] cells) {
    var tag = CsvAnalysisFormat.text(header, cells, "transformer_tag");
    if (tag == null) {
      throw new CsvLineException(line.number(), "transformer_tag is empty");
    }
    var transformerId = transformerId(tag);
    if (transformerId == null) {
      throw new CsvLineException(line.number(), "transformer " + tag + " does not exist or is decommissioned");
    }
    var form = new AnalysisForm();
    form.setTransformerId(transformerId);
    form.setSampleDate(CsvAnalysisFormat.date(header, cells, "sample_date"));
    form.setH2(CsvAnalysisFormat.number(header, cells, "h2"));
    form.setCh4(CsvAnalysisFormat.number(header, cells, "ch4"));
    form.setC2h2(CsvAnalysisFormat.number(header, cells, "c2h2"));
    form.setC2h4(CsvAnalysisFormat.number(header, cells, "c2h4"));
    form.setC2h6(CsvAnalysisFormat.number(header, cells, "c2h6"));
    form.setCo(CsvAnalysisFormat.number(header, cells, "co"));
    form.setCo2(CsvAnalysisFormat.number(header, cells, "co2"));
    form.setO2(CsvAnalysisFormat.number(header, cells, "o2"));
    form.setN2(CsvAnalysisFormat.number(header, cells, "n2"));
    return form;
  }

  private Long transformerId(String tag) {
    return transformerIds.computeIfAbsent(tag.toUpperCase(Locale.ROOT), key ->
       transformers.findByTagIgnoreCase(key)
          .filter(t -> t.getStatus() != DECOMMISSIONED)
          .map(TransformerEntity::getId)
          .orElse(null));
  }
}
