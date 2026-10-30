package com.ppossatto.tensio.domain.report;

/// A generated report ready to be downloaded.
///
/// @param fileName suggested file name, including the extension
/// @param content  the file bytes
public record ReportFile(String fileName, byte[] content) {
}
