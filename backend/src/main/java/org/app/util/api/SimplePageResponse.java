package org.app.util.api;

import java.util.*;

public record SimplePageResponse<T>(
        int pageNoRequest,
        int pageSizeRequest,
        int numOfPagedElements,
        int numOfTotalElements,
        boolean hasNext,
        List<T> pagedElements
) {

}
