package study.datajpa.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ObjectUtils.isEmpty;
import static study.datajpa.entity.QMember.*;
import static study.datajpa.entity.QTeam.team;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import study.datajpa.dto.querydsl.MemberDto;
import study.datajpa.dto.querydsl.MemberSearchCondition;
import study.datajpa.dto.querydsl.MemberTeamDto;
import study.datajpa.dto.querydsl.QMemberDto;
import study.datajpa.dto.querydsl.QMemberTeamDto;
import study.datajpa.dto.querydsl.UserDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.QMember;
import study.datajpa.entity.QTeam;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    /**
     * #jpa-springboot Spring에서 EntityManager는 Thread-Safe하다.
     *
     * 스프링 프레임워크에서는 EntityManager를 프록시로 집어 넣는다. 이후 각각의 트랜잭션마다 별도의 영속성 컨텍스트를 바인딩한다.
     * 따라서 여러 쓰레드에서 동시에 같은 EntityManager에 접근해도 동시성 문제가 없다.
     *
     * JPAQueryFactory의 동시성 문제는 같이 사용되는 EntityManager에 달려 있기 때문에 아래와 같이 JPAQueryFactory를 필드에 놓아도 동시성 문제는 발생하지 않는다.
     *
     * 참고 - ORM 표준 JPA 책 13.1 트랜잭션 범위의 영속성 컨텍스트
     */
    private JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        this.queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startQuerydsl() {
        //member1을 찾아라.
        /**
         * #querydsl QMember의 2가지 사용법
         *
         * 1. QMember qMember = new QMember("m") m은 alias
         * 같은 테이블을 join하는 경우나, 서브쿼리에서 같은 테이블을 사용하는 경우에 테이블을 구분하기 위해 이 방식 사용
         *
         * 2. QMember qMember = QMember.member
         * 기본적으로 이 방법을 사용하고 QMember를 static import해서 깔끔하게 사용할 수 있다.
         */
        Member findMember = queryFactory
                .select(member) //QMember.member에서 QMember를 static import했음,
                .from(member)
                .where(member.name.eq("member1"))//파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getName()).isEqualTo("member1");
    }


    /**
     * #querydsl 검색 - where 조건문
     *
     * member.name.eq("member1") // name = 'member1'
     * member.name.ne("member1") //name != 'member1'
     * member.name.eq("member1").not() // name != 'member1'
     * member.name.isNotNull() //이름이 is not null
     * member.age.in(10, 20) // age in (10,20)
     * member.age.notIn(10, 20) // age not in (10, 20)
     * member.age.between(10,30) //between 10, 30
     * member.age.goe(30) // age >= 30
     * member.age.gt(30) // age > 30
     * member.age.loe(30) // age <= 30
     * member.age.lt(30) // age < 30
     * member.name.like("member%") //like 검색
     * member.name.contains("member") // like ‘%member%’ 검색
     * member.name.startsWith("member") //like ‘member%’ 검색
     */
    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.name.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        //또는 and는 그냥 ,로도 가능하다. 이 경우 null 값은 무시한다.
//        Member findMember = queryFactory
//                .selectFrom(member)
//                .where(member.name.eq("member1"),
//                        (member.age.eq(10)))
//                .fetchOne();

        assertThat(findMember.getName()).isEqualTo("member1");
    }

    /**
     * #querydsl 결과 조회 방법
     *
     * fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
     * fetchOne() : 단 건 조회
     * - 결과가 없으면 : null
     * - 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
     *
     * fetchFirst() : limit(1).fetchOne()
     */
    @Test
    public void fetch() {
        //List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();
        //단 건
        Member findMember1 = queryFactory
                .selectFrom(member)
                .fetchOne();

        //처음 한 건 조회
        Member findMember2 = queryFactory
                .selectFrom(member)
                .fetchFirst();
    }

    /**
     * #querydsl 정렬
     *
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.name.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getName()).isEqualTo("member5");
        assertThat(member6.getName()).isEqualTo("member6");
        assertThat(memberNull.getName()).isNull();
    }

    /**
     * #querydsl 페이징
     *
     * count 쿼리는 따로 작성해서 날려야 한다. 이 때 count 쿼리는 count만 세면 되므로 최대한 단순하게 날리자.
     */
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.name.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    /**
     * #querydsl 집합
     *
     * - JPQL
     * select COUNT(m), SUM(m.age), AVG(m.age), MAX(m.age), MIN(m.age) from Member m
     */
    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * #querydsl 조인 기본
     *
     * - 기본 문법
     * join(조인 대상, 별칭으로 사용할 Q타입)
     * - fetch join
     * join(member.team, team).fetchJoin();
     * - on 절로 필터링
     * join(member.team, team).on(team.name.eq("teamA"))
     *
     * - 예제
     * 팀 A에 소속된 모든 회원 찾기
     */
    @Test
    public void join() throws Exception {
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        assertThat(result)
                .extracting("name")
                .containsExactly("member1", "member2");
    }

    /**
     * #querydsl 세타조인 - 연관관계 없는 엔티티 외부 조인
     *
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.name = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.name = t.name
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.name.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }

    /**
     * #querydsl 서브쿼리
     *
     * 서브쿼리는 com.querydsl.jpa.JPAExpressions를 사용해서 Select문을 만들어주어야 한다.
     * static import를 쓰면 더 깔끔하게 사용 가능하다.
     *
     * - 한계점
     * From절에서는 서브쿼리가 불가능하다. 이 때는
     * 1. join으로 변경
     * 2. 애플리케이션에서 분리해서 쿼리 날리기
     * 3. nativeSQL을 사용
     *
     * 하지만 굳이 억지로 이렇게 From 절에 쿼리를 넣지 않고 애플리케이션 단에서 풀어낼 수 있으면 그렇게 하는 것이 좋다.
     * DB는 최대한 데이터를 최소화해서 가져오는데 집중하자. 그 이유는
     * 1. 쿼리 재사용에 좋다.
     * 2. DB 부하를 줄여준다. (DB는 WAS보다 확장도 어렵고 성능도 떨어진다.)
     * 3. 유지 보수에 좋다.
     * - 관심사의 분리(화면에 관한 것은 화면단에서, 계산은 애플리케이션단에서)
     * - 이해가 쉬움(집합적으로 사고해야 해서 어려운 SQL과 달리 프로그래밍 로직은 이해하기 훨씬 좋다.)
     *
     * - 예제
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * #querydsl CASE문
     *
     * 웬만하면 CASE문으로 화면에 관한 것을 처리하지 말고 화면단에서 처리하게 넘기자
     */
    @Test
    public void caseSimple() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
    }

    @Test
    public void caseComplicated() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
    }

    /**
     * #querydsl Order by + Case문
     *
     * 예를 들어서 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
     * 1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
     * 2. 0 ~ 20살 회원 출력
     * 3. 21 ~ 30살 회원 출력
     */
    @Test
    public void caseWithOrderBy() {
        // Querydsl은 자바 코드로 작성하기 때문에 rankPath 처럼 복잡한 조건을 변수로 선언해서 select 절,orderBy 절에서 함께 사용할 수 있다.
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);

        List<Tuple> result = queryFactory
                .select(member.name, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();

        for (Tuple tuple : result) {
            String name = tuple.get(member.name);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("name = " + name + " age = " + age + " rank = " + rank);
        }
    }

    /**
     * #querydsl 상수, 문자 더하기
     */
    @Test
    public void constant() {
        Tuple result = queryFactory
                .select(member.name, Expressions.constant("A"))
                .from(member)
                .fetchFirst();
    }

    @Test
    public void concat() {
        String result = queryFactory
                .select(member.name.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.name.eq("member1"))
                .fetchOne();

        /*
          결과 - member1_10
          참고: member.age.stringValue() 부분이 중요한데, 문자가 아닌 다른 타입들은 stringValue() 로 문자로 변환할 수 있다. 이 방법은 ENUM을 처리할 때도 자주 사용한다.
        */
    }

    /**
     * #querydsl 프로젝션 - 하나 or 튜플
     */
    @Test
    public void projectionSingle() {
        List<String> result = queryFactory
                .select(member.name)
                .from(member)
                .fetch();
    }

    @Test
    public void projectionTuple() {
        List<Tuple> result = queryFactory
                .select(member.name, member.age)
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            String name = tuple.get(member.name);
            Integer age = tuple.get(member.age);
            System.out.println("name=" + name);
            System.out.println("age=" + age);
        }
    }

    /**
     * #querydsl 프로젝션 - DTO 반환
     *
     * 순수 JPQL을 사용할 경우 지저분하고 생성자 방식만 지원한다.
     *
     * querydsl의 경우 프로퍼티, 필드, 생성자 방식을 모두 지원한다.
     */
    @Test
    public void projectionDto() {
        //JPQL 예시 - 지저분
        //em.createQuery( "select new study.datajpa.dto.querydsl.MemberDto(m.name, m.age) from Member m", MemberDto.class);

        //setter 방식
        List<MemberDto> setter = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();

        //field 직접 접근 방식
        List<MemberDto> field = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();

        //setter나 field 접근 방식에서 별칭이 다를 때
        QMember memberSub = new QMember("memberSub");
        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.name.as("username"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(memberSub.age.max())
                                        .from(memberSub), "age") // select절 서브쿼리에 별칭 주기
                ))
                .from(member)
                .fetch();

        //생성자 사용
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.name,
                        member.age))
                .from(member)
                .fetch();

    }

    /**
     * #querydsl 프로젝션 - DTO 반환 with @QueryProjection
     *
     * @QueryProjection을 DTO의 생성자 위에 붙여주면 훨씬 깔끔하게 사용 가능하다. {@link MemberDto#MemberDto(String, int)}
     * 거기다 컴파일 타임에 문법 오류까지 잡아주므로 좋다.
     *
     * 그러나 DTO가 QueryDSL에 의존하게 된다는 단점이 있다.
     * 만약 QueryDSL을 쓰지 않게 될 경우 DTO에서 이 어노테이션을 떼어내야 하고 DTO는 Layer에서 쓰이고 있기 때문에 그 영향이 커질 수 있다.
     *
     * - 주의 : ./gradlew compileQuerydsl을 해서 QMemberDto가 생성된 것을 확인할 것
     */
    @Test
    public void queryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.name, member.age))
                .from(member)
                .fetch();
    }

    /**
     * #querydsl 동적쿼리
     *
     * 1. BooleanBuilder 이용 - 가끔 사용한다.
     * 2. Where 다중 파라미터 사용 - 자주 사용한다. 훨씬 가독성에 좋고 BooleanExpression을 재사용 가능하다.
     *
     * {@link #before()} 사전 데이터 확인
     */
    @Test
    public void 동적쿼리_BooleanBuilder() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.name.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void 동적쿼리_WhereParam() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond)) // where조건에 null값은 무시된다.
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return StringUtils.hasText(usernameCond) ? member.name.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    //자주 사용하는 BooleanExpression은 하나로 묶어서 재사용해보자
    private BooleanExpression isValid(String usernameCond, Integer ageCond) {
        if (StringUtils.hasText(usernameCond)) {
            return ageEq(ageCond);
        }

        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    /**
     * #querydsl 수정, 삭제 벌크 연산
     *
     * 주의 : 배치 쿼리를 실행하고 나면 영속성 컨텍스트를 flush() clear()해줄 것
     */
    @Test
    void updateBulk() {
        long count = queryFactory
                .update(member)
                .set(member.name, "비회원")
                .where(member.age.lt(28))
                .execute();

        //영속성 컨텍스트 초기화는 배치 쿼리 후 필수
        em.flush();
        em.clear();

        long count2 = queryFactory
                .update(member)
                .set(member.age, member.age.add(1)) // 곱하기는 add 대신multiply()
                .execute();

        long count3 = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }

    /**
     * #querydsl SQL function 호출
     */
    @Test
    void sqlFunction() {
        // member -> M으로 변경하는 replace 함수 사용
        String result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2}",
                        member.name, "member", "M"))
                .from(member)
                .fetchFirst();

        //소문자로 변경해서 비교하기
        String result2 = queryFactory
                .select(member.name)

                .from(member)
//                .where(member.name.eq(Expressions.stringTemplate("function('lower', {0})", member.name)))
                .where(member.name.eq(member.name.lower())) // lower 같은 ansi 표준은 querydsl이 내장 중
                .fetchFirst();
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

    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }
}
