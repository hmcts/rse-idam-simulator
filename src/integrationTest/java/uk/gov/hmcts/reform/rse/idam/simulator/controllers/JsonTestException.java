package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

public class JsonTestException extends RuntimeException {

    public static final long serialVersionUID = 5528143;

    public JsonTestException(String errorMessage, Throwable causeException) {
        super(errorMessage, causeException);
    }
}
