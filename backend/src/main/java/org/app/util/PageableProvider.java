package org.app.util;

import org.springframework.data.domain.*;

public interface PageableProvider {

    Pageable pageable(int pageNo, int pageSize);

}
