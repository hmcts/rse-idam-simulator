package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

public class JsonTestException extends RuntimeException {
    public JsonTestException(String errorMessage, Throwable causeException) {
        super(errorMessage, causeException);
    }
}
