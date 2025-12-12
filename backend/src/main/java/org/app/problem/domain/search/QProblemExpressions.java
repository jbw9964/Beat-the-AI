package org.app.problem.domain.search;

import org.app.entity.*;

// 만약 query dsl join 에서 alias 가 동일하지 않으면
// 의도하지 않은 추가 join 이 생길 수 있음. 기본적으로 jqpl 의 "지칭 다름" 으로 인한 join 생성이랑 동일.
// 그래서 좀 global 하게 공유할 수 있는 expression 들 모아둠.

// "지칭 다름" 으로 인한 join 생성되는 jpql 예시 : 대충 이런식
// select m, m.team from Member m
// left join m.team as t on t.name = :teamName

// 이거 피하려면 아래처럼 만들어야 됨. 정말 작동하는지는 몰?루 암튼 보면 뭐가 잘못됬는지 암.
// select m, t from Member m        <-- m.team 이 아니라 t 로 지칭
// left join m.team as t on t.name = :teamName
public class QProblemExpressions {

    public static QProblem QPROBLEM_ROOT = QProblem.problem;
    public static QProblemAggregation QPROBLEM_AGGREGATION_ROOT
            = QProblemAggregation.problemAggregation;
    public static QProblemAggregation QP__QPROBLEM_AGGREGATION_TARGET
            = QPROBLEM_ROOT.problemAggregation;

}
