package org.app.entity;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ScenarioInfo {

    @Setter(AccessLevel.PUBLIC)
    private int scenarioOrder;

    private String scenarioContent;

    private String answerContent;

    public ScenarioInfo(@NonNull String scenarioContent) {
        if (scenarioContent.isEmpty()) {
            throw new IllegalArgumentException("scenarioContent cannot be empty");
        }

        this.scenarioContent = scenarioContent;
    }

    public ScenarioInfo(String scenarioContent, String answerContent) {
        this(scenarioContent);
        this.answerContent = answerContent;
    }


}
