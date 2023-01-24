package com.example.jpaplayground;

import com.example.jpaplayground.jpql.Human;
import com.example.jpaplayground.jpql.HumanDto;
import com.example.jpaplayground.jpql.Mentor;
import com.example.jpaplayground.jpql.Room;
import com.example.jpaplayground.valuetype.Address;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JPQLTest {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpql");
    EntityManager em;
    EntityTransaction tx;
    @BeforeEach
    void init() {
        em = emf.createEntityManager();
        tx = em.getTransaction();
        tx.begin();
    }

    @AfterEach
    void finish() {
        em.flush();
        em.clear();
        tx.rollback();
    }


    /**
     * #jpabasic/jpql jpql에서 select 쿼리의 동작 방식
     *
     * EntityManager.find()를 통해 조회할 경우 영속성 컨텍스트를 먼저 확인 후 없으면 DB 접근. DB 접근해서 가져 온 Entity는 영속성 컨텍스트에 등록
     * 반면에 EntityManager.createQuery() 즉 JPQL을 통해 조회할 경우 일단 DB에서 조회(SELECT 쿼리 나감) 후 영속성 컨텍스트에 동일한 Entity 있을시 DB에서 가져온 Entity 폐기 후 영속성 컨텍스트의 Entity 반환
     */

    @Test
    void parameterBinding() {
        // 파라미터 바인딩 : 동적으로 쿼리 속 변수 조정 가능
        List<Human> humans = em.createQuery("select h from Human h where h.age = :age", Human.class)
                .setParameter("age", 19)
                .getResultList();
    }

    /**
     * #jpabasic/jpql 프로젝션 - jpql에서 select 절에 어떤 것을 가져올지 정하는 것
     */

    //Entity 프로젝션 : Entity로 가져오는 애들은 모두 영속성 컨텍스트에 저장된다.
    @Test
    void entityProjection() {
        List<Human> humans = em.createQuery("select h from Human h", Human.class)
                .getResultList();
    }
    //Entity 프로젝션 with join
    //엔티티와 연관된 엔티티를 프로젝션 할 때는 명시적으로 join을 적어주는 것이 가독성에 좋고 쿼리튜닝에 편하다.
    @Test
    void entityProjectionWithJoin() {
        List<Room> rooms = em.createQuery("select r from Human h join h.room r where h.id=1",
                Room.class).getResultList();

        //이 방식은 묵시적으로 join이 나가기 때문에 사용 x
        //List<Group> groups = em.createQuery("select h.group from Human h",Group.class).getResultList();

    }

    //ValueType 프로젝션
    @Test
    void valueTypeProjection() {
        Human human = new Human();
        human.setAddress(new Address());
        em.persist(human);

        Address address = em.createQuery("select h.address from Human h where h.id = 1", Address.class)
                .getSingleResult(); // 리턴 값이 명확히 한 개일 때만 사용, 없거나 더 많을 경우 예외가 난다.
    }

    //스칼라 타입 프로젝션 : 일반 값 타입들을 따로 따로 가져와서 특별히 매칭할 엔티티가 없는 프로젝션,
    //DTO로 받지 않고 그냥 처리할 수도 있지만(Object[]로 반환됨), 웬만하면 DTO로 받는 것이 깔끔하다.
    @Test
    void scalaProjection() {
        List<HumanDto> humanDto =
                em.createQuery("select new com.example.jpaplayground.jpql.HumanDto(h.name, h.age) from Human h", HumanDto.class)
                .getResultList();
    }

    /**
     * #jpabasic/jpql 페이징 - limit offset 등 방언에 맞게 SQL을 변환해서 날려준다.
     */
    @Test
    void paging() {
        List<Human> resultList = em.createQuery("select h from Human h order by h.age desc",
                        Human.class)
                .setFirstResult(2) // 두번째부터
                .setMaxResults(10) // 10개까지
                .getResultList();
    }

    /**
     * #jpabasic/jpql join
     *
     * Join : 항상 명시적으로 join을 쓸 것(ex. "select t from Member m join m.team t". 묵시적으로 쓰게 되면 (ex. "select m.team from Member")
     * 컬렉션에 대하여 경로 탐색도 불가능하고 직관적이기 않기 때문에 쿼리 튜닝도 어렵다.
     */
    @Test
    void join() {
        //내부 조인
        em.createQuery("select h from Human h join h.room", Human.class)
                .getResultList();

        //외부 조인
        em.createQuery("select h from Human h left join h.room", Human.class)
                .getResultList();


        //on절을 활용하여 필터링 join(JPA 2.1부터 지원)
        em.createQuery("select h from Human h join h.room r on h.name=r.name", Human.class).getResultList();
    }

    //세타 조인 : 연관관계가 없을 때 서로 조인. 현재 Human과 SuperMarket은 연관관계가 없는 상황
    @Test
    void thetaJOin() {
        em.createQuery("select h from Human h, SuperMarket s where h.name=s.name", Human.class)
                .getResultList();

        //on절을 활용하면 다음과 같이 할 수 있다.(하이버네이트5.1부터 가능)
        em.createQuery("select h from Human h join SuperMarket s on h.name=s.name", Human.class).getResultList();

        //on절을 활용하면 세타 조인시 외부 조인도 가능하다.
        em.createQuery("select h from Human h left join SuperMarket s on h.name=s.name", Human.class).getResultList();
    }

    /**
     * #jpabasic/jpql fetch join : 해당 엔티티와 연관 엔티티의 정보를 한방에 가져오는 방법
     *
     * 그냥 FetchType.Eager를 쓸 경우 JPQL을 써서 가져올 때 N + 1 문제가 발생한다.
       그렇다고 FetchType.Lazy로 쓰자니 갖고 오자마자 컬렉션에 대해서 루프를 돌면 마찬가지로 N + 1 문제가 발생한다.
       이럴 때 Fetch Join을 쓰면 된다.

       따라서 정석적인 로딩 전략은 글로벌 로딩 전략을 Lazy로 바르고 Eager가 필요할 때 Fetch Join을 쓴다.

      한계
       1. 페치 조인 대상에는 별칭을 줄 수 없음. 따라서 별칭을 이용해서 Filtering(ex.where m.id=1) 불가능.
       2. 둘 이상의 컬렉션은 페치 조인 불가능.
       3. 컬렉션을 페치 조인할 경우 페이징 불가능. 왜냐하면 데이터가 뻥튀기 되기 때문에 페이징하기 애매함.
          이 경우 그냥 반대로 바꾸어서 다대일 페치 조인으로 풀어내거나, @BatchSize을 이용해서 페이징 할 수 있다.
     */
    //다대일 상황
    @Test
    void fetchJoinManyToOne() {
        em.createQuery("select m from Mentor m join fetch m.human")
                .getResultList();
    }
    /**
     * #jpabasic/jpql 일대다 상황에서 fetchjoin
     *
     * 이 때는 데이터가 뻥튀기가 된다. 따라서 페이징이 불가능하다.
     * 자세히 설명하자면, A,B 2개의 데이터가 뻥튀기 되면 A1, A2, B1, B2가 된다. 이 때 페이징으로 1부터 3개 가져오라하면 A2, B1, B2를 가져오게 된다.
     * 실제로 우리가 원하는 거는 뻥튀기 된 데이터에서 중복을 제거한 후에 그 중에서 페이징하는 것이지만 위와 같이 다르게 동작한다.
     *  (이해 안간다면 스프링 부트와 JPA 2편, 주문 조회V3, 22분을 볼 것)
     *
     * 또한 Team(1) : Member(N) : Order(N) 같은 상황 같이 둘 이상의 컬렉션에 대해서 페치 조인을 사용하면 안된다.
     * 왜냐하면 걷잡을 수 없이 데이터가 늘어나며 JPA가 데이터를 맞추는데 실패할 가능성이 있기 때문이다.
     *
     * 뻥튀기되어 중복되는 엔티티를 제거하기 위해서는 distinct를 써주면 됨. JPA가 알아서 식별자 중복되는 것을 제거해준다.                         k
     *
     * - 페이징을 하려면 fetch join을 하지 않고 batchsize 방식을 사용하기
     */
    @Test
    void fetchJoinOneToMany() {
        em.createQuery("select distinct h from Human h join fetch h.mentors")
                .getResultList();
    }

    /**
     * #jpabasic/jpql batchSize 예제
     * 
     * {@link jpabook.jpashop.api.OrderApiController#ordersV3_page(int, int)} ;}
     */
    @Test
    void batchSize() {
        for (int i = 0; i < 5; i++) {
            Human human = new Human();
            em.persist(human);

            Mentor mentor = new Mentor();
            mentor.setHuman(human);
            em.persist(mentor);
        }

        em.flush();
        em.clear();

        List<Human> findHumans = em.createQuery("select h from Human h",Human.class)
                .getResultList();

        for (Human findHuman : findHumans) {
            List<Mentor> mentors = findHuman.getMentors();

            mentors.get(0).getName();
        }
    }

    /**
     * #jpabasic/jpql 서브쿼리
     *
     * from절에서는 서브쿼리가 안되기 때문에 웬만하면 join으로 풀고 정 안되면 네이티브 쓸 것.
     */
    @Test
    void subquery() {
        em.createQuery("select h from Human h where exists (select r from h.room r where r.name = '팀A')").getResultList();
    }

    /**
     * #jpabasic/jpql 네임드 쿼리
     *
     * @see Human
     */
    @Test
    void namedQuery() {
        em.createNamedQuery("Human.findByName", Human.class)
                .setParameter("name", "jiwoo")
                .getResultList();
    }

    /**
     * #jpabasic/jpql 벌크 업데이트
     *
     * 여러 건을 한번에 업데이트하고 싶을 때 사용, JPA가 제공하는 변경 감지는 한계가 있음.
     *
     * 벌크 연산은 변경 감지와 달리, 영속성 컨텍스트를 무시하고 DB에 직접 쿼리한다.
     * 따라서
     * 1.트랜잭션 시작 후 벌크 연산을 가장 먼저 수행하거나,
     * 2.벌크 연산을 수행했다면 영속 컨텍스트를 초기화 한 후 엔티티를 다시 가져와서 사용한다.
     */
    @Test
    void bulkUpdate() {
        Human human = new Human();
        human.setAge(5);
        em.persist(human);

        int resultCount = em.createQuery(
                        "update Human h set h.age = h.age + 1 where h.age < :age") // 결과는 영향받는 엔티티 수 반환
                .setParameter("age", 10)
                .executeUpdate();

        //현 상황에서 DB에는 업데이트되어 있지만 이 human객체와 영속성 컨텍스트는 age + 1로 업데이트가 되어 있지 않음
        //따라서 이 때는 영속성 컨텍스트를 초기화하고
        em.clear();
        //새로 가져오기
        Human newHuman = em.find(Human.class, human.getId());
    }
}
