package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.analysis.GasChart;
import com.ppossatto.tensio.domain.analysis.GasProfile;
import com.ppossatto.tensio.domain.diagnosis.LatestDiagnosis;
import com.ppossatto.tensio.domain.report.ReportFile;
import com.ppossatto.tensio.domain.transformer.Transformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Generates the transformer data sheet as PDF from the `reports/transformer.jrxml` JasperReports template.
///
/// The template receives `java.lang.String` parameters, already formatted for display (`null` when the
/// value is unknown), plus two `java.util.Collection` parameters with the gas chart rows. Their names are
/// the `PARAM_*` constants of this class. Each chart row is a map with the keys `CHART_*`:
/// gas label (String), series name (String) and value as % of the gas reference (Double).
@Service
@RequiredArgsConstructor
@Slf4j
public class TransformerReportService {

  private static final String TEMPLATE = "reports/transformer.jrxml";
  private static final DateTimeFormatter GENERATED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  public static final String PARAM_TAG = "TAG";
  public static final String PARAM_SERIAL_NUMBER = "SERIAL_NUMBER";
  public static final String PARAM_STATUS = "STATUS";
  public static final String PARAM_MANUFACTURER = "MANUFACTURER";
  public static final String PARAM_MANUFACTURE_YEAR = "MANUFACTURE_YEAR";
  public static final String PARAM_COMMISSIONING_DATE = "COMMISSIONING_DATE";
  public static final String PARAM_CRITICALITY = "CRITICALITY";
  public static final String PARAM_RATED_POWER = "RATED_POWER";
  public static final String PARAM_PRIMARY_VOLTAGE = "PRIMARY_VOLTAGE";
  public static final String PARAM_SECONDARY_VOLTAGE = "SECONDARY_VOLTAGE";
  public static final String PARAM_OIL_TYPE = "OIL_TYPE";
  public static final String PARAM_SUBSTATION_ACRONYM = "SUBSTATION_ACRONYM";
  public static final String PARAM_SUBSTATION_NAME = "SUBSTATION_NAME";
  public static final String PARAM_SUBSTATION_LOCATION = "SUBSTATION_LOCATION";
  public static final String PARAM_HEALTH_STATUS = "HEALTH_STATUS";
  public static final String PARAM_FINAL_FAULT = "FINAL_FAULT";
  public static final String PARAM_LAST_SAMPLE_DATE = "LAST_SAMPLE_DATE";
  public static final String PARAM_GENERATED_AT = "GENERATED_AT";
  public static final String PARAM_COMBUSTIBLE_GASES = "COMBUSTIBLE_GASES";
  public static final String PARAM_NON_COMBUSTIBLE_GASES = "NON_COMBUSTIBLE_GASES";

  public static final String CHART_GAS = "gas";
  public static final String CHART_SERIES = "series";
  public static final String CHART_VALUE = "value";
  public static final String SERIES_LATEST = "Latest analysis";
  public static final String SERIES_AVERAGE = "12-month average";

  private final TransformerService transformers;
  private final GasProfileService gasProfiles;

  /// Compiled template, created on first use and reused afterwards.
  private volatile JasperReport report;

  /// Builds the PDF data sheet of a transformer.
  ///
  /// @param id the transformer id
  /// @return the PDF file named `transformer-<tag>.pdf`
  /// @throws org.springframework.web.server.ResponseStatusException with 404 when the transformer does not exist
  public ReportFile transformerPdf(Long id) {
    var transformer = transformers.find(id);
    var diagnosis = transformers.latestDiagnosis(id).orElse(null);
    var gasProfile = gasProfiles.profile(id);
    try {
      var print = JasperFillManager.fillReport(report(), parameters(transformer, diagnosis, gasProfile),
         new JREmptyDataSource(1));
      var pdf = JasperExportManager.exportReportToPdf(print);
      return new ReportFile("transformer-" + transformer.tag() + ".pdf", pdf);
    } catch (JRException e) {
      throw new IllegalStateException("Could not generate the PDF of transformer " + id, e);
    }
  }

  private JasperReport report() throws JRException {
    var compiled = report;
    if (compiled == null) {
      synchronized (this) {
        compiled = report;
        if (compiled == null) {
          compiled = compile();
          report = compiled;
        }
      }
    }
    return compiled;
  }

  private static JasperReport compile() throws JRException {
    try (var template = new ClassPathResource(TEMPLATE).getInputStream()) {
      log.info("Compiling report template {}", TEMPLATE);
      return JasperCompileManager.compileReport(template);
    } catch (IOException e) {
      throw new IllegalStateException("Report template not found: " + TEMPLATE, e);
    }
  }

  private static Map<String, Object> parameters(Transformer t, LatestDiagnosis diagnosis, GasProfile gasProfile) {
    var substation = t.substation();
    Map<String, Object> params = new HashMap<>();
    params.put(PARAM_TAG, t.tag());
    params.put(PARAM_SERIAL_NUMBER, t.serialNumber());
    params.put(PARAM_STATUS, text(t.status()));
    params.put(PARAM_MANUFACTURER, t.manufacturer());
    params.put(PARAM_MANUFACTURE_YEAR, text(t.manufactureYear()));
    params.put(PARAM_COMMISSIONING_DATE, text(t.commissioningDate()));
    params.put(PARAM_CRITICALITY, text(t.criticality()));
    params.put(PARAM_RATED_POWER, withUnit(t.ratedPowerMva(), "MVA"));
    params.put(PARAM_PRIMARY_VOLTAGE, withUnit(t.primaryVoltageKv(), "kV"));
    params.put(PARAM_SECONDARY_VOLTAGE, withUnit(t.secondaryVoltageKv(), "kV"));
    params.put(PARAM_OIL_TYPE, text(t.oilType()));
    params.put(PARAM_SUBSTATION_ACRONYM, substation.acronym());
    params.put(PARAM_SUBSTATION_NAME, substation.name());
    params.put(PARAM_SUBSTATION_LOCATION, location(substation.city(), substation.state()));
    if (diagnosis != null) {
      params.put(PARAM_HEALTH_STATUS, text(diagnosis.healthStatus()));
      params.put(PARAM_FINAL_FAULT, diagnosis.finalFault() == null ? null : diagnosis.finalFault().getLabel());
      params.put(PARAM_LAST_SAMPLE_DATE, text(diagnosis.sampleDate()));
    }
    params.put(PARAM_GENERATED_AT, LocalDateTime.now().format(GENERATED_AT_FORMAT));
    params.put(PARAM_COMBUSTIBLE_GASES, chartRows(gasProfile.combustible()));
    params.put(PARAM_NON_COMBUSTIBLE_GASES, chartRows(gasProfile.nonCombustible()));
    return params;
  }

  /// One row per gas and series; values without data are left out so the chart skips them.
  private static List<Map<String, ?>> chartRows(GasChart chart) {
    List<Map<String, ?>> rows = new ArrayList<>();
    addSeries(rows, chart.labels(), SERIES_LATEST, chart.latest());
    addSeries(rows, chart.labels(), SERIES_AVERAGE, chart.average());
    return rows;
  }

  private static void addSeries(List<Map<String, ?>> rows, List<String> labels, String series,
                                List<BigDecimal> values) {
    for (int i = 0; i < labels.size(); i++) {
      var value = values.get(i);
      if (value != null) {
        rows.add(Map.of(CHART_GAS, labels.get(i), CHART_SERIES, series, CHART_VALUE, value.doubleValue()));
      }
    }
  }

  private static String text(Object value) {
    return value == null ? null : value.toString();
  }

  private static String withUnit(BigDecimal value, String unit) {
    return value == null ? null : value.stripTrailingZeros().toPlainString() + " " + unit;
  }

  private static String location(String city, String state) {
    if (city == null) {
      return state;
    }
    return state == null ? city : city + " / " + state;
  }
}
