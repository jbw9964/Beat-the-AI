package org.app.user.dto.response;

import org.app.user.dto.*;
import org.app.util.api.*;

public record GetPublicRecordResponse(
        DetailedPlayRecordInfo detailedPlayRecordInfo,
        SimplePageResponse<ScenarioRecordInfo> scenarioPageResponse,
        boolean isMine
) {

}
