package org.app.user.dto.response;

import java.util.*;
import org.app.user.dto.*;

public record GetPublicRecordResponse(
        DetailedPlayRecordInfo detailedPlayRecordInfo,
        List<ScenarioRecordInfo> scenarioInfos,
        boolean isMine
) {

}
