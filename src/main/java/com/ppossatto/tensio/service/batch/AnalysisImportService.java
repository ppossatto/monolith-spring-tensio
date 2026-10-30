package com.ppossatto.tensio.service.batch;

import com.ppossatto.tensio.domain.diagnosis.DiagnosedAnalysis;
import com.ppossatto.tensio.repository.AnalysisRepository;
import com.ppossatto.tensio.repository.TransformerRepository;
import com.ppossatto.tensio.service.AnalysesDiagnosedEvent;
import com.ppossatto.tensio.service.AnalysisService;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/// Imports analyses from a CSV file with Spring Batch: one chunk-oriented step reads the lines, validates them
/// ({@link CsvAnalysisProcessor}) and stores each valid analysis with its diagnosis through
/// {@link AnalysisService#store}. Invalid lines are skipped and reported. When the job ends, the summary
/// e-mail goes to the user who uploaded the file.
///
/// Imports run one at a time on a dedicated background thread: the default Spring Boot job repository keeps
/// executions in memory and is not meant for concurrent jobs.
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisImportService {

  /// Lines stored per transaction.
  private static final int CHUNK_SIZE = 100;

  private final JobRepository jobRepository;
  private final JobOperator jobOperator;
  private final PlatformTransactionManager transactionManager;
  private final AnalysisService analysisService;
  private final TransformerRepository transformers;
  private final AnalysisRepository analyses;
  private final Validator validator;
  private final ApplicationEventPublisher events;

  /// Runs the imports one after the other, outside the HTTP request.
  private final ExecutorService executor = Executors.newSingleThreadExecutor(
     Thread.ofPlatform().name("analysis-import").daemon(true).factory());

  /// Checks the header of an uploaded file before starting the import.
  ///
  /// @param content the file content
  /// @throws IllegalArgumentException when the file is empty or a required column is missing
  /// @throws IOException              when the file cannot be read
  public void checkHeader(InputStream content) throws IOException {
    try (var reader = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8))) {
      var first = reader.readLine();
      if (first == null) {
        throw new IllegalArgumentException("The file is empty");
      }
      CsvAnalysisFormat.header(first);
    }
  }

  /// Queues the import of a file; it runs in the background and the file is deleted afterwards.
  ///
  /// @param file      the uploaded file, saved to a temporary location
  /// @param fileName  original file name, shown in the e-mail
  /// @param recipient e-mail of the user who uploaded it
  public void importFile(Path file, String fileName, String recipient) {
    executor.execute(() -> runImport(file, fileName, recipient));
  }

  /// Stops accepting imports when the application shuts down; a running import is interrupted.
  @PreDestroy
  void shutdown() {
    executor.shutdownNow();
  }

  private void runImport(Path file, String fileName, String recipient) {
    List<DiagnosedAnalysis> results = Collections.synchronizedList(new ArrayList<>());
    List<String> errors = Collections.synchronizedList(new ArrayList<>());
    try {
      var status = run(file, results, errors);
      if (status != BatchStatus.COMPLETED) {
        errors.add("The import stopped with status " + status + "; lines after the last stored chunk were not"
           + " imported.");
      }
    } catch (Exception e) {
      log.error("Import of {} failed", fileName, e);
      errors.add("The import failed: " + e.getMessage());
    } finally {
      deleteQuietly(file);
    }
    log.info("Import of {} by {}: {} analyses stored, {} lines rejected", fileName, recipient, results.size(),
       errors.size());
    events.publishEvent(new AnalysesDiagnosedEvent(recipient, fileName, List.copyOf(results), List.copyOf(errors)));
  }

  private BatchStatus run(Path file, List<DiagnosedAnalysis> results, List<String> errors) throws Exception {
    var processor = new CsvAnalysisProcessor(transformers, analyses, validator);
    var reader = new FlatFileItemReaderBuilder<CsvLine>()
       .name("analysisCsvReader")
       .resource(new FileSystemResource(file))
       .encoding(StandardCharsets.UTF_8.name())
       .linesToSkip(1)
       .skippedLinesCallback(processor::header)
       .lineMapper((line, number) -> new CsvLine(number, line))
       .build();
    ItemWriter<CsvAnalysis> writer = chunk -> {
      for (var item : chunk) {
        results.add(analysisService.store(item.form()));
      }
    };
    var step = new StepBuilder("importAnalyses", jobRepository)
       .<CsvLine, CsvAnalysis>chunk(CHUNK_SIZE)
       .transactionManager(transactionManager)
       .reader(reader)
       .processor(processor)
       .writer(writer)
       .faultTolerant()
       .skip(CsvLineException.class)
       .skipLimit(Long.MAX_VALUE)
       .skipListener(new SkipListener<CsvLine, CsvAnalysis>() {
         @Override
         public void onSkipInProcess(CsvLine item, Throwable t) {
           errors.add(t.getMessage());
         }
       })
       .build();
    var job = new JobBuilder("analysisImport", jobRepository).start(step).build();
    var parameters = new JobParametersBuilder()
       .addString("file", file.toString())
       .addLocalDateTime("startedAt", LocalDateTime.now())
       .toJobParameters();
    return jobOperator.start(job, parameters).getStatus();
  }

  private static void deleteQuietly(Path file) {
    try {
      Files.deleteIfExists(file);
    } catch (IOException e) {
      log.warn("Could not delete the temporary file {}", file, e);
    }
  }
}
