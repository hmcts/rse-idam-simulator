package uk.gov.hmcts.reform.rse.idam.simulator.service.memory;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Builder
@Getter
public class SimObject {

    private String clientId;
}
