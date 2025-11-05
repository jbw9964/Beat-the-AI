package org.app.user.event;

import org.springframework.modulith.*;

@NamedInterface
public record UserWithdrawEvent(
        Long userId
) {

}
