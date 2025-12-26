package org.app.problem.dto.response;

import org.app.problem.dto.*;
import org.app.util.api.*;

public record GetProblemRewardsResponse(
        SimplePageResponse<ProblemRewardInfo> pageResponse,
        boolean isMine
) {

}
