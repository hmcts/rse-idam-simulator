package uk.gov.hmcts.reform.rse.idam.simulator.token;

public class TokenGenerationException extends RuntimeException {

    private static final long serialVersionUID = 9294532L;

    public TokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
