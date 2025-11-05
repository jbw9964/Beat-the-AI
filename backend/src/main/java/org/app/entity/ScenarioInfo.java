package org.app.entity;

import lombok.*;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ScenarioInfo {

    private int scenarioOrder;

    private String scenarioContent;

    private String answerContent;

    public ScenarioInfo(int scenarioOrder, @NonNull String scenarioContent) {
        if (scenarioOrder < 0) {
            throw new IllegalArgumentException("scenarioOrder should be greater or equal to 0");
        }

        if (scenarioContent.isEmpty()) {
            throw new IllegalArgumentException("scenarioContent cannot be empty");
        }

        this.scenarioOrder = scenarioOrder;
        this.scenarioContent = scenarioContent;
    }

    public ScenarioInfo(
            int scenarioOrder, String scenarioContent, String answerContent
    ) {
        this(scenarioOrder, scenarioContent);
        this.answerContent = answerContent;
    }


}
