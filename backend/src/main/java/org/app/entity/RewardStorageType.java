package org.app.entity;

import lombok.*;

@RequiredArgsConstructor
public enum RewardStorageType {
    DB(Names.DB),
    SERVER(Names.SERVER),
    AWS_S3(Names.AWS_S3),
    ;

    public final String value;

    public static class Names {

        public static final String
                DB = "DATABASE",
                SERVER = "SERVER",
                AWS_S3 = "AWS_S3";
    }
}
