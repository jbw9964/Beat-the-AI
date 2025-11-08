package org.app.user.dto.response;

import java.util.*;
import org.app.user.dto.*;

public record GetMyRecordResponse(
        DetailedPlayRecordInfo detailedPlayRecordInfo,
        List<ScenarioRecordInfo> scenarioRecordInfos
) {

}
