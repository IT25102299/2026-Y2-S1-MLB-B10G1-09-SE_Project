package com.serenitystay.hotel.report;

import java.io.IOException;

/**
 * DESIGN PATTERN: Strategy (Behavioral) - Managerial Dashboard & Analytics.
 *
 * Each report FORMAT (Word, Excel, CSV) is an interchangeable algorithm that
 * turns the same {@link SalesReportData} into a file. SalesReportService picks
 * one at run time from the requested format. A new format (e.g. PDF) is just a
 * new implementing class; SalesReportService does not change.
 */
public interface ReportGenerator {

    /** Format key used in the URL, e.g. "docx", "xlsx", "csv". */
    String format();

    /** HTTP content type of the generated file. */
    String contentType();

    /** File extension without the dot. */
    String fileExtension();

    /** Builds the report file. */
    byte[] generate(SalesReportData data) throws IOException;
}

