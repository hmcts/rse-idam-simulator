package uk.gov.hmcts.reform.rse.idam.simulator;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CaseDataUtilsTest {

    @Test
    public void givenDataWithCaseLink_thenReturnLinkValue() {
        assertThat("Toto should be himself","Toto", is("Toto"));
    }

    @Test
    public void notFinishedAssertion() {
        assertThat("Toto should be be an issue");
    }

}
