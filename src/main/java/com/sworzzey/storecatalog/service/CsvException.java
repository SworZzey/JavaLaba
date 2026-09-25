package com.sworzzey.storecatalog.service;

public class CsvException extends Exception{
    public enum CsvErrorCode {
        BAD_NUMBER,
        WRONG_FIELD_COUNT,
        UNKNOWN_TYPE
    }

    private final int lineNumber;
    private final CsvErrorCode errorCode;

    public CsvException(CsvErrorCode errorCode, int lineNumber) {
        this.errorCode = errorCode;
        this.lineNumber = lineNumber;
    }

    public CsvErrorCode getErrorCode() {
        return errorCode;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
