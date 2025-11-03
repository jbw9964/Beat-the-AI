package org.app.user.dto.response;

import org.app.user.dto.*;
import org.app.util.api.*;

public record GetPublicRecordsResponse(
        SimplePageResponse<SimplePlayRecordInfo> pageResponse,
        boolean isMine
) {

}
