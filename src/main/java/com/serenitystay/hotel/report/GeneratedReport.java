package com.serenitystay.hotel.report;

/** A finished report ready to be sent to the browser. */
public record GeneratedReport(byte[] content, String contentType, String fileName) {
}

