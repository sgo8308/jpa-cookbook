package study.datajpa.repository;

import static org.springframework.util.ObjectUtils.isEmpty;
import static study.datajpa.entity.QMember.member;
import static study.datajpa.entity.QTeam.team;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import study.datajpa.dto.querydsl.MemberSearchCondition;
import study.datajpa.dto.querydsl.MemberTeamDto;
import study.datajpa.dto.querydsl.QMemberTeamDto;
import study.datajpa.entity.Member;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Member> findUserByNameWithMybatis(String name) {

        //대충 마이바티스 이용한다고 생각
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }


    /**
     * #querydsl 페이징 - spring data jpa의 Page와 Pageable을 같이 쓰기
     */
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
            Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.name,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ).fetchOne();

        return new PageImpl<>(content, pageable, total);

        /*
          Count 쿼리 최적화 - count 쿼리가 생략 가능한 경우 생략해서 처리
          페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
          마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
         */
//        JPAQuery<Member> countQuery = queryFactory
//                .select(member)
//                .from(member)
//                .leftJoin(member.team, team)
//                .where(usernameEq(condition.getUsername()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe()));
//        return PageableExecutionUtils.getPage(content, pageable,
//                countQuery::fetchOne);
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return StringUtils.hasText(usernameCond) ? member.name.eq(usernameCond) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }

    /**
     * #querydsl 페이징 - spring data jpa의 Page와 Pageable을 같이 쓰기 with 정렬
     *
     * QueryDSL을 쓸 때 Pageable의 Sort를 이용하려면 복잡한 방식을 사용해야 한다.
     * QuerytdslRepositorySupport를 직접 구현해서 편한 방식으로 만들 수도 있다.
     *
     * {@link study.datajpa.querydsl.QuerydslBasicTest#searchPageOrder()} 에서 쿼리 확인
     */
    public Page<Member> searchPageOrder(Pageable pageable) {
        List<Member> content = queryFactory
                .selectFrom(member)
                .orderBy(order(pageable.getSort())) // 이 부분 주목
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, 10); //total은 예제 간소화르 위해 임의로 10을 넣음
    }

    private OrderSpecifier<?>[] order(Sort sort) { // 여러 정렬 조건이 있을 수 있으므로 배열로 반환
        List<OrderSpecifier<?>> list = new ArrayList<>();

        sort.stream().forEach(order -> { // Sort는 내부에 List<Order>를 갖고 있고 이를 대상으로 Stream 가능
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) { //어떤 칼럼이 정렬 기준으로 왔는지에 따라 다른 OrderSpecifier 반환
                case "name":
                    list.add(new OrderSpecifier<>(direction, member.name));
                    break;
                case "age":
                    list.add(new OrderSpecifier<>(direction, member.age));
                    break;
            }
        });

        return list.toArray(OrderSpecifier[]::new);
    }
}
