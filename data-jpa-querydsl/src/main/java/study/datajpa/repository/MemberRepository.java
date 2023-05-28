package study.datajpa.repository;

/**
 * #datajpa org.springframework.data.jpa.repository는 JPA 전용 패키지
 *
 * org.springframework.data는 jpa 뿐만이 아니라 mongo, redis 등 여러 DB가 포함되어 있으며 공통된 기능을 제공한다.
 * 이를 테면 JpaRepository 인터페이스는 PagingAndSortingRepository를 상속하고 있는데, 이 인터페이스는
 * org.springframework.data.repository 안에 있다.
 * 따라서 RDB가 아니라 MongoDB를 쓸 대도 이 인터페이스를 그대로 사용 가능하다.
 *
 * 하지만 운영중인 DB를 바꾸는 일은 흔하지 않기 때문에 그냥 비슷한 인터페이스로 개발 가능하다는 장점으로 생각하면 좋다.
 */

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.datajpa.MemberDto;
import study.datajpa.entity.Member;

/**
 * #datajpa JpaRepository<대상 엔티티, PK 타입>에 대한 설명
 *
 * JpaRepository를 상속받은 인터페이스를 주입받을 경우 Spring Data JPA가 자동으로 구현체를 직접 만들어서 주입한다.
 * JpaRepository는 일반적으로 엔티티 대상으로 사용되는 대부분의 공통 메소드의 인터페이스를 제공하고 주입된 구현체가 구현하고 있다.
 * JpaRepository의 구현체는 @Transactional이 걸려 있다. 따라서 서비스 계층의 트랜잭션을 전파받거나 혹은 외부 트랜잭션이 없는 경우 직접 트랜잭션을 생성해서 진행한다.
 *
 * @see org.springframework.data.jpa.repository.support.SimpleJpaRepository JPARepositrory 구현체 중 하나
 */
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom{

    /**
     * #datajpa 메소드 이름으로 쿼리 생성 - 이름과 나이를 기준으로 회원을 조회하려면?
     *
     * Spring Data JPA에서는 관례에 따라 메소드의 이름으로 웬만한 쿼리를 생성 가능하다.
     * 하지만 복잡해질수록 메소드 이름이 길어지는 문제가 있다.
     * 이 방식 이외에 나머지 방식은 파라미터에 @Param을 붙여야 한다.
     *
     * 참고 - https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
     */
    List<Member> findByNameAndAgeGreaterThan(String name, int age);

    /**
     * #datajpa @Query로 인터페이스 위에 바로 쿼리 때려박기
     *
     * 실제로는 이름 없는 NamedQuery와 동일하다. 따라서 컴파일 시점에 문법 오류를 잡아준다.
     * 예를 들어 아래에 m.name을 m.username으로 바꾸면 에러가 뜬다.
     *
     * - 간단할 때는 윗 방식을 사용하고 조금 복잡할 때는 이 방식을 사용하자.
     */
    @Query("select m from Member m where m.name = :name and m.age =:age")
    List<Member> findUser(@Param("name") String name, @Param("age") int age);

    /**
     * #datajpa @Query DTO로 받기
     *
     * 다음과 같이 사용하지만 QueryDSL을 사용하면 번거로운 new ~는 필요없다.
     */
    @Query("select new study.datajpa.dto.datajpa.MemberDto(m.id, m.name, t.name) " +
            "from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * #datajpa in 문법과 컬렉션을 파라미터로 받기
     */
    @Query("select m from Member m where m.name in :names")
    List<Member> findByAges(@Param("names") List<String> names);

    /**
     * #datajpa 반환타입을 지정하면 알아서 그에 맞게 Spring Data JPA가 변화해준다.
     * 직관적으로 사용하면 된다.
     *
     * 참고 - https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types
     */
    List<Member> findListByName(String name); //컬렉션 - 결과가 0개일 경우 빈 리스트를 반환한다.

    Member findMemberByName(String name); //단건 - 2개 이상일 경우 예외, 0개일 경우 Null

    Optional<Member> findByName(String name); //단건 Optional


    /**
     * #datajpa 페이징
     *
     * return type에 Page인터페이스와 파라미터로 Pageable을 받으면 페이징 메소드로 추가된다.
     * 반환된 Page인터페이스 구현체는 페이징과 관련된 수많은 기능을 제공해준다.
     * 구체적인 사용법은 테스트 코드를 참고 할것
     */
    Page<Member> findByAge(int age, Pageable pageable); //전체 갯수를 의미하는 count쿼리가 자동으로 나간다.

    //Slice<Member> findByAge(String name, Pageable pageable); //더보기 방식의 페이징할 때 사용, count 쿼리 사용안함, 내부적으로 limit + 1을 이용해 다음 페이지 유무 확인
    //List<Member> findByAge(String name, Pageable pageable); //그냥 리스트로만 받고 싶을 때, count 쿼리 사용 안함
    //List<Member> findByAge(String name, Sort sort);

    /**
     * #datajpa 페이징 - Count 쿼리 최적화
     *
     * left join이 많고 where문으로 필터링하지 않는 실제 쿼리를 할 때는 count 쿼리를 join해서 가져오는 것은 불필요하고 성능에 좋지 않다.
     * 이 때는 count 쿼리를 분리해줄 수 있다.
     */
    @Query(value = "select m from Member m left join m.team",
            countQuery = "select count(m) from Member m") // 조인을 쓰지 않고 단순하게 쿼리
    Page<Member> findByName(String name, Pageable pageable);

    /**
     * #datajpa 벌크 업데이트 in JPA
     *
     * 여러 건을 한번에 업데이트하는 경우 변경 감지로는 너무 많은 쿼리가 나가게 된다. 이 때 벌크 업데이트를 사용한다.
     *
     * - Modifying
     *
     *   이 어노테이션을 넣어주어야 마지막에 createQuery()이후에 .executeUpdate()를 해서 정상적으로 벌크 업데이트가 나간다.
     *
     *   - clearAutomatically
     *
     *     벌크 업데이트는 영속성 컨텍스트를 무시하고 바로 DB에 쿼리를 집어넣기 때문에 영속성 컨텍스트와 실제 DB와의 불일치가 생겨서 find했을 때
     *     실제 DB에 업데이트된 내용이 아니라 영속성 컨텍스트에서 데이터를 찾음으로써 문제가 생길 수 있다.
     *     따라서 영속성 컨텍스트를 clear해주는 작업이 필요한데 이것을 자동으로 해주는 옵션이다.
     *
     *   - flushAutomatically
     *
     *     일반적으로 JPQL 쿼리를 날릴 때 영속성 컨텍스트에 쓰지 지연 저장소의 저장된 쿼리를 flush한다.
     *     그러나 전체 쿼리가 아니라 해당 JPQL에 연관된 쿼리만 flush하게 되는데 이 때 clear를 해서 비워버리면 flush되지 않은 쿼리가 drop되는 문제가 있다.
     *     그래서 clear 전에 flush를 자동으로 해주는 옵션이다.
     *
     *     - 참고 : https://www.inflearn.com/questions/188207/modifying%EC%97%90-%EA%B4%80%ED%95%9C-%EC%A7%88%EB%AC%B8
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age > :age")
    int bulkAgePlus1GreaterThan(@Param("age") int age);


    /**
     * #datajpa @EntityGraph - fetch join을 data jpa에서 편리하게 쓰자. 쿼리가 간단할 땐 @EntityGraph 복잡할 땐 fetch join
     *
     * fetch join으로 left outer join해서 나간다.
     */
    @Override
    @EntityGraph(attributePaths = "team")
    List<Member> findAll(); // JpaRepository가 기본으로 제공하는 메소드도 오버라이드하여 EntityGraph사용 가능

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph(); //JPQL + 엔티티 그래프

    @EntityGraph(attributePaths = "team")
    List<Member> findEntityGraphByName(String name);

    /**
     * #datajpa Hint를 사용한 극한의 최적화 - 거의 쓸 일 없음
     *
     * JPA(표준)과 달리 Hibernate(구현체)는 여러 최적호 요소를 갖고 있다. JPA가 Hibernate에게 주는 Hint를 통해 이 최적화를 이용할 수 있다.
     *
     * 아래 예시는 readOnly를 주어서 조회시 스냅샷을 찍지 않고, 변경 감지를 진행하지 않게해서 성능최적화를 하게 한다.
     * 하지만 보통 이정도까지 최적화하기 전에 캐싱을 쓰거나 다른 방식으로 넘어가기 때문에 거의 쓸 일 없다.
     */
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByName(String name);

    /**
     * #datajpa Lock - for update를 쓰는 비관적 락 또는 낙관적 락
     *
     * <@Version을 쓰면 되는데 LockModeType.OPTIMISTIC이 존재하는 이유>
     *
     * Entity에 @Version이 있는 칼럼만 넣어도 낙관적 락이 동작한다. LockModeType.NONE은 아무 의미 없다.
     * @Version이 존재하면 @Lock이 있는 쿼리로 Entity를 조회하지 않았더라도 Update가 일어날 때 버전을 같이 체크한다.
     *
     * - 그럼 LockModeType.OPTIMISTIC이 있는 쿼리는 뭐가 다를까?
     * Entity를 조회하고 수정을 하지 않더라도 항상 커밋하기 직전에 SELECT로 버전을 가져와서 처음 가져왔을 때의 버전과 같은지 비교한다.
     * 그리고 버전이 달라졌다면 예외를 터뜨린다.
     *
     * - 왜 이렇게 동작할까?
     * 만약 트랜잭션 진행 도중 다른 트랜잭션이 동일한 엔티티에 수정했다면 예외를 일으키게 되고
     * 이렇게 동작함으로써 DIRTY-READ와 NON-REPEATABLE-READ를 방지할 수 있기 때문이다.(NONE 모드라면 수정을 해야만 버전 체크를 해서 조회만 할 경우엔 앞 문제를 방지할 수 없다.)
     *
     * - 한계점
     * 하지만 어차피 DB의 Isolation Level은 최소 READ COMMITTED 이상이고 영속성 컨텍스트를 통해서 JPA가 자체적으로 REPEATABLE READ 또한 제공해주기 때문에
     * 위와 같은 효과가 굳이 필요한가 싶다. 따라서 단순히 @Version 칼럼만 추가해주는 것만으로 낙관적 락을 구현하기 충분하다고 생각한다.
     */
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Member> findWithLockById(Long id);

    /**
     * #datajpa Projection - 원하는 칼럼만 콕 찝어서 갖고오고 싶을 때
     */
    List<NameOnly> findNameById(Long id); //Close Projection 인터페이스 방식 - 원하는 칼럼을 getter로 갖는 인터페이스를 정의 후 리턴 값에 반영

    List<MemberNameDto> findNameDtoById(Long id); //Close Projection Dto 방식 - 원하는 칼럼을 생성자의 파라미터로 갖는 클래스를 정의 후 리턴 값에 반영

    <T> List<T> findNameWithGenericById(Long id, Class<T> type); // 제네릭을 이용해 동적으로 프로젝션도 가능하다.

    interface NameOnly{ //보기 쉽게 내부 인터페이스로 만들엇지만 따로 빼도 상관없음

        //아래와 같이 @Value를 통해 Spel을 섞어서 사용할 수도 있다.
        //대신에 이 경우 엔티티에 필드를 콕 찝어서 가져오는게 아니라 다 가져오므로 최적화가 되지 않는다.
        //@Value("#{target.name + ' ' + target.age + ' ' + target.team.name}") // getName할 시 "jiwoo 15 팀1"과 같이 반환
        String getName();
    }

    @Getter
    class MemberNameDto{
        private String name;

        public MemberNameDto(String name) {
            this.name = name;
        }
    }

    /**
     * #datajpa 네이티브 쿼리 - JPQL이나 QueryDSL로 해결이 안되면서 정적쿼리일 때 사용, 동적쿼리는 Mybatis나 JDBCTemplate 사용
     *
     * 제약
     * Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음(믿지 말고 직접 처리)
     * JPQL처럼 애플리케이션 로딩 시점에 문법 확인 불가
     * 동적 쿼리 불가
     */
    @Query(value = "select * from member where ?", nativeQuery = true)
    Member findMemberNativeByName(String name);
    @Query(value = "SELECT m.member_id as id, m.name, t.name as teamName " +
            "FROM member m left join team t ON m.team_id = t.team_id",
            countQuery = "SELECT count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);

    interface MemberProjection {
        String getId();
        String getName();
        String getTeamName();
    }
}

