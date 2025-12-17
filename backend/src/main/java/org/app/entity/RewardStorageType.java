package org.app.entity;

import lombok.*;

@RequiredArgsConstructor
public enum RewardStorageType {
    LOCAL_STORAGE(Names.LOCAL_STORAGE),
    AWS_S3(Names.AWS_S3),
    ;

    public final String value;

    public static class Names {

        public static final String
                LOCAL_STORAGE = "LOCAL_STORAGE",
                AWS_S3 = "AWS_S3";
    }
}
