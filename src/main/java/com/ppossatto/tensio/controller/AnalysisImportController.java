package com.ppossatto.tensio.controller;

import com.ppossatto.tensio.service.batch.AnalysisImportService;
import com.ppossatto.tensio.service.batch.CsvAnalysisFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

/// CSV import of analyses (`/analyses/import`): upload page and upload handling. The import itself runs in
/// the background with Spring Batch ({@link AnalysisImportService}); the summary is e-mailed to the user.
@Controller
@RequestMapping("/analyses/import")
@RequiredArgsConstructor
@Slf4j
class AnalysisImportController {

  private final AnalysisImportService imports;

  /// Shows the upload page with the expected format.
  ///
  /// @param model the view model
  /// @return the `analyses/import` view
  @GetMapping
  String page(Model model) {
    model.addAttribute("requiredColumns", CsvAnalysisFormat.REQUIRED);
    model.addAttribute("optionalColumns", CsvAnalysisFormat.OPTIONAL);
    return "analyses/import";
  }

  /// Checks the header of the uploaded file, saves it to a temporary file and starts the import.
  ///
  /// @param file      the uploaded CSV file
  /// @param principal the signed-in user, recipient of the summary
  /// @param model     the view model
  /// @param redirect  flash attributes for the next page
  /// @return a redirect to the analysis list, or the `analyses/import` view with the error
  @PostMapping
  String upload(@RequestParam("file") MultipartFile file, Principal principal, Model model,
                RedirectAttributes redirect) {
    if (file.isEmpty()) {
      return error(model, "Choose a non-empty CSV file.");
    }
    var fileName = file.getOriginalFilename() == null ? "analyses.csv" : file.getOriginalFilename();
    try {
      imports.checkHeader(file.getInputStream());
      var copy = Files.createTempFile("tensio-import-", ".csv");
      try (var content = file.getInputStream()) {
        Files.copy(content, copy, StandardCopyOption.REPLACE_EXISTING);
      }
      imports.importFile(copy, fileName, principal.getName());
    } catch (IllegalArgumentException e) {
      return error(model, e.getMessage());
    } catch (IOException e) {
      log.error("Could not read the uploaded file {}", fileName, e);
      return error(model, "The file could not be read.");
    }
    redirect.addFlashAttribute("message", "Import of " + fileName + " started. Each valid line is stored with its"
       + " diagnosis; the summary will be sent to " + principal.getName() + ".");
    return "redirect:/analyses";
  }

  private String error(Model model, String message) {
    page(model);
    model.addAttribute("error", message);
    return "analyses/import";
  }
}
