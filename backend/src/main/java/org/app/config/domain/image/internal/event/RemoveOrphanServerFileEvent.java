package org.app.config.domain.image.internal.event;

import java.util.*;

public record RemoveOrphanServerFileEvent(
        List<String> removals
) {

}
