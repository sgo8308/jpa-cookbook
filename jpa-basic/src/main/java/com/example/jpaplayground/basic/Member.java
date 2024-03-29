package com.example.jpaplayground.basic;


import com.example.jpaplayground.inheritance.BaseEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import jdk.jfr.Frequency;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

/**
 * #jpabasic jpa에 대한 기본을 알려주는 클래스
 */
@Entity // JPA가 관리하는 엔티티라는 표시, 참고로 캐싱할 때 엔티티를 캐싱하면 안 되고 DTO로 변환해서 캐싱해야 한다.
@Table(name = "member") // 테이블 이름이 다를 경우
@SequenceGenerator( // PK의 GeneratedValue 전략을 Sequence로 했을 경우 필요한 어노테이션, 시퀀스에 관한 정보를 정의
  name = "MEMBER_SEQ_GENERATOR",  // 시퀀스 식별자
  sequenceName = "MEMBER_SEQ", // 매핑할 데이터베이스 시퀀스 이름
  initialValue = 1,
  allocationSize = 50 // 몇 개까지 미리 시퀀스를 땡겨올지, 미리 땡긴 후 그 다음부터는 DB가 아니라 메모리에서 시퀀스를 가지고 옴으로써 매 번 네트워크를 타고 시퀀스를 가져와야 하는 비용을 아낄 수 있다.
)
/**
 * #jpabasic @DynamicInsert 데이터 삽입 시 Null이라면 쿼리에 포함하지 않기
 *
 * 기본 값이 있는 경우에 객체를 저장할 때 그 칼럼에 대해서는 값을 세팅하지 않는 경우가 있다.
 * 이 때 JPA는 그런 것을 상관하지 않고 그 칼럼은 Null 값으로 세팅하여 insert를 진행한다.
 * @DynamicInsert를 사용하면 Null값인 칼럼은 제외하고 insert 쿼리를 날린다.
 * @DynamicUpdate를 사용하면 Null값인 칼럼은 제외하고 update 쿼리를 날린다.
 *
 * 이 방식 이외에도 @PrePersist를 이용할 수 있다.
 *
 *  @PrePersist
 *  public void prePersist() {
 *      this.likeCount = this.likeCount == null ? 0 : this.likeCount;
 *  }
 */
@DynamicInsert
public class Member extends BaseEntity {

    @Id
    /**
     * #jpabasic @GeneratedValue 자동으로 PK값을 생성하게 해주는 어노테이션
     *
     *
     * - IDENTITY 전략
     * DB에 맡기는 전략. auto_increment로 동작한다.
     * PK값을 DB에 Insert해야만 알 수 있기 때문에 entityManager.persist()를 하는 순간 Insert 쿼리가 바로 날라간다.

     * - SEQUENCE 전략
     * Sequence 사용, MySQL은 Sequence 테이블을 만들게 됨
     * entityManager.persist()를 하는 순간 쿼리를 날리지는 않지만 Sequence 값을 얻어오고, 이 값을 PK로 entity를 1차 캐시에 등록한다.

     * - TABLE 전략
     * Sequence를 테이블로 만들어서 사용, 성능상 좋지 않기 때문에 거의 쓰이지 않음.

     * - AUTO 전략
     * 데이터베이스 방언에 따라 위 3가지 전략을 자동으로 선택한다.
     */
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_SEQ_GENERATOR")
    private Long id;

    @Column( //컬럼에 관한 옵션을 준다. 가급적이면 제약 조건을 다 적어주자. 제약 조건을 여기다 적어주면 DB를 까보지 않고도 알 수 있기 때문에 좋다.
            name = "name", // 컬럼명이 다를 때 맞춰주기 위하여
            insertable = true, // 삽입이 가능한지,
            updatable = true, // 수정이 가능한지
            unique = false, length = 10, columnDefinition = "varchar(100) default 'EMTPY'") // 위 옵션 외에 나머지는 애플리케이션이 처음 실행될 때 DDL 생성하는데만 영향을 준다.
    private String userName;

    @Column(columnDefinition = "integer default 1") // 기본값 넣는 방법
//    @ColumnDefault("1")
    private Integer age;

    @Enumerated(EnumType.STRING) //Enum type을 DB에 매핑할 때 DDL 자동 생성하는데 쓰임, Ordinal은 데이터가 꼬일 수 있으므로 절대 쓰지 말 것
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP) // 날짜 타입을 매핑할 때, DATE는 날짜만 TIME은 시간만 , TIMESTAMP는 둘 다, DDL 자동 생성시 쓰임, LocalDate, LocaDateTime에는 쓸 필요 없음
    private Date createdDate;

    @Lob // 매우 긴 문자열이 필요할 때, 매핑하는 필드 타입이 문자면 CLOB으로 매핑되고 나머지는 BLOB으로 매핑
    private String description;

    @Transient // ddl에 반영 안되게 하고 싶을 때
    private int temp;

    /**
     * #jpabasic 다대일 연관 관계 매핑, 연관관계 주인에 대한 설명과 실전 팁
     *
     *
     * 연관관계를 매핑해줄 때 사용, 현재 엔티티가 다대일 관계에서 다 쪽에 해당할 경우 Many로 시작
     * 양방향 매핑의 경우 다대일 관계에서 '일'에 해당하는 부분에도 참조 변수가 들어가게 된다 'List<Member> members' 처럼

     * 이 때 연관관계의 주인을 정해야 한다.
     * 연관관계의 주인이란 어떤 엔티티가 실제로 '외래키'의 관리를 담당할 것인가를 말한다. 주인의 값을 변경할 때만 외래키가 업데이트된다.
     * 그냥 두 엔티티에서 외래키 업데이트를 다 진행할 수 있으면 되지 않나? 라고 생각할 수 있다. JPA 입장에서 헷갈리기 때문에 안된다.
     * 예를 들어 Member.setTeam(TeamA) 이런 식으로 Member는 팀을 세팅했는데 Team은 값을 세팅 안한 경우, JPA는 외래키를 넣어야 할지 말아야할지 알 수 없다.
     *
     * 주인을 정하기 위해서는 연관관계의 주인이 아닌 쪽에 @OneToMany(mappedBy = "team")처럼 mappedBy 옵션을 명시해주면 된다.

     * 연관관계의 주인이 아닌 쪽은 값을 읽기만 가능하고 등록이나 수정 등은 하지 못한다.
     * Member와 Team의 관계에서 Member가 주인이라면 Member.setTeam()할 때만 외래키가 업데이트되고 Team.getMembers().addMember()는 외래키에 영향주지 않는다.
     * 즉 트랜잭션 안에서 members.add(new Member(1L, "some")등을 하더라도 db에 반영되지 않는다.
     * 오직 Member에서 setTeam을 해야 한다.

     * 다대일 관계에서 연관관계의 주인은 누구나 될 수 있지만 Many쪽에서 연관관계 주인을 가져가는 것이 좋다. 여기서는 Member가 Many 쪽이다.
     * 왜냐하면 실제 DB에는 Member 테이블에 외래키가 들어 있을 것이기 때문이다. Member 엔티티를 수정하거나 등록했을 때 Member 테이블이 업데이트 되는 것이
     * 직관적이고 이해하기 쉽다. 그렇지 않다면 Team 엔티티를 수정했는데(Team.getMembers().addMember()) Member 쪽으로 update 쿼리가 나가게 되고 이는 직관적이지 않다.

     * - 실전 팁

     * 먼저 다대일 단방향 매핑으로 다 진행하고 정말 필요할 때 양방향 매핑을 맺도록 하자. 왜냐하면 양방향 매핑은 신경써야 할 게 많다.
     * 이를 테면 toString()에서 무한루프가 발생 안되도록 해주어야 하고, 연관관계 편의 메서드(changeTeam)도 정의해야 한다.
     *
     * - 양방향 매핑은 언제 쓸까?
     *
     *  복잡한 조회 쿼리 진행시 양방향 매핑이 편리할 때가 있다.
     *  예를 들어 team 1:N member 이런 관계가 있을 때 team과 member를 fetch join으로 한번에 조회하고 싶은 경우다.

     * * 일대다 양방향도 JPA에서 공식적으로 지원하는 것은 아니지만 만들 수 있다.
     * 이 때는 @OneToMany쪽에 mappedBy를 빼고 @JoinColumn을 넣어주고,
     * @ManyToOne에서는 @JoinColumn(name = "team_id", insertable = false, updatable = false)로 해주면 된다.
     */
    @ManyToOne(fetch = FetchType.LAZY) // 기본은 지연 로딩으로 바르고 즉시 로딩이 필요하면 fetch join 사용
    @JoinColumn(name = "team_id") //team이란 객체는 team_id라는 외래키 칼럼으로 관계를 맺고 있다고 선언해준다.
    private Team team;

    /**
     * #jpabasic 일대다 단방향 연관 관계
     *
     * 일대다 단방향 연관관계는 Team 쪽에 'List<Member> members'로 연관관계를 맺고 Member 쪽에는 아무런 Team에 참조를 갖지 않는 것
     * 웬만하면 이 관계는 사용하지 않는게 좋다. 왜냐하면 Team을 만들고 members에 member를 추가한 후 insert한다고 해보자.
     * 이 때 일단 Team을 넣는 insert 쿼리가 한 번 나가고, 연관된 Member의 외래키를 방금 넣은 team의 id로 변경하는 update 쿼리가 추가적으로 나간다.
     * 따라서 비효율적이며 Team에만 손댔는데 Member 테이블에 쿼리가 나가므로 직관적이지 않다.
     * 다대일 단방향일 경우 쿼리 한 번에 해결되고 직관적이다.
     *
       일대다 단방향 연관관계의 경우 @JoinColumn을 사용하지 않으면 외래키가 아니라 연결테이블을 사용하려 하므로 꼭 선언해야 한다.
       다대일 단방향의 경우에는 생략하면 외래키 칼럼을 추론해서 이용하기도 함.
     */

    /**
     * #jpabasic @OneToOne 일대일 연관 관계를 매핑
     *
     * 일대일 관계는 DB에서 외래키에 Unique 제약 조건을 걸어 놓은 형태
     * 가장 중요한 것은 어떤 테이블에 외래키를 위치시킬 것인가?
     * 두 테이블이 있다면 주 테이블과 대상 테이블로 나누고 상황에 따라 외래키를 위치시키자.

       일반적으로 주 테이블에 놓는 것이 나은데, 그 이유는

       - 주 테이블을 자주 Select하고 Select할 때 지연 로딩을 적용할 수 있어 성능상 장점이 있음.

       주 테이블에 경우에는 주 테이블을 읽으면서 locker가 null인지 아닌지 바로 확인 가능하다.
       하지만 대상 테이블에 놓을 경우 locker에 대해서 null을 넣을지 프록시를 넣을지 정하기 위해 결국에는 Locker 테이블을 확인해야 한다.
       즉 이미 조회를 진행했기 때문에 지연 로딩에 의미가 없어지므로 JPA는 즉시 로딩으로 처리해 버린다.

       한 편 대상 테이블에 외래키를 넣을 경우 추후에 Member 한 명이 여러 Locker를 가질 수 있게 비즈니스 요구사항이 변할 경우
       별다른 변경 없이 외래키의 Unique 제약 조건만 해제해 주면 되기 때문에 변경에 용이하다.
       하지만 반대로 하나의 Locker에 여러 Member가 들어갈 수 있게 변경되면 오히려 주 테이블에 놓는게 더 변경이 유리하기 때문에 이 부분은 대동소이.
    */
    @OneToOne
    @JoinColumn(name = "locker_id")
    private Locker locker;

    /**
     * #jpabasic Cascade, OrphanRemoval
     *
     * - Cascade : 부모를 entityManager.persist() 또는 삭제 할 때 자식도 같이 하게 해주어 편리하게 해주는 옵션
     *
     *
     * Cascade.ALL의 경우 저장과 삭제 둘 다.
     * 반드시 부모와  자식이 완전히 종속된 관계에서만 사용한다.
     * 다른 엔티티가  이 자식을 알고 있다면 사용하면 안됨. 특정 엔티티가 개인 소유할 때 !
     * orphanRemovel은 부모가 삭제 되었을 때 뿐만이 아니라 관계가 끊어졌을 때도 자식이 삭제 된다는 점에서 차이가 있다.
     *
     * Cascade.PERSIST의 경우 저장만 같이.
     *
     * - orphanRemoval : 부모가 없어진 엔티티는 DB에 delete 쿼리가 나가게끔 동작시키는 옵션.
     * 예를 들어 balls.remove(2) 할 경우 2번 인덱스의 자식은 고아가 되고 delete 쿼리가 나가게 된다.
     *
     * 반드시 특정 엔티티가 개인 소유할 때만 조심해서 써야 한다. 안 그러면 다른 엔티티도 참조하고 있는 엔티티인데
     * 어느 엔티티에 의하여 잘못 삭제될 수도 있기 때문이다.
     * 또한 어떤 엔티티를 저장할 때, 연관된 어떤 엔티티들이 함께 저장될까? 를 계속 코드를 보며 추적해야 하는 단점이 있다.
     * 따라서 웬만하면 쓰지 않는 것이 권장된다.
     * 하지만 DDD 진행시 Aggregate Root 개념과 관련하여 요긴하게 쓸 수도 있다.
    */
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ball> balls = new ArrayList<>(); // 관례상 바로 초기화를 해주는 것이 좋다. 그래야 예기치 못한 Null예외가 터지지 않는다.

    /**
     * #jpabasic 일대다 단방향
     *
     * 일대다 단방향 관계는 피하는 것이 좋다.
     * 왜냐하면 외래키는 Like 테이블에 존재하지만 이 외래키를 관리하는 주체는 Member이기 때문에 직관적이지 않아 유지보수가 어렵다.
     * 또 Like는 Member를 모른다. 따라서 insert시 member_id를 같이 넣을 수 없다. 항상 Member가 likes.add()할 때만 Like 테이블에 update 쿼리를 날리는 방식으로 동작한다.
     *
     * 즉 유지보수에 어렵고 추가적인 update 쿼리가 나가기 때문에 일대다 단방향은 피하는 것이 좋고, 이럴 때는 객체지향적으로 조금 손해를 보더라도 다대일 양방향 관계로 가져가자.
     */
    @OneToMany
    @JoinColumn(name = "member_id")
    private List<Like> likes = new ArrayList<>(); // 관례상 바로 초기화를 해주는 것이 좋다. 그래야 예기치 못한 Null예외가 터지지 않는다.


    /**
     * #jpabasic 연관 관계 편의 메서드 - 양방향 연관 관계에서만 필요하다.
     *
     * 만약 양방향 연관관계를 맺는다면 아래와 같은 식으로 양쪽 다 값을 세팅해주어야 한다. 그렇지 않으면 헷갈리고 꼬일 수 있기 떄문이다.
     * 또 이 로직은 단순히 setTeam()으로 하지 않고 changeTeam()과 같이 의미를 부여해서 정의하는 것이 좋다.
     * 왜냐하면 setTeam() 일반적인 setter로 오해될 수 있기 때문에 team.getMembers().add(this);를 중복으로 적게될 수 있다.
     * 연관관계 편의 메서드는 Member와 Team 둘 중에 한 쪽에만 만든다. 왜냐하면 잘못하면 무한루프 걸릴 수도 있고 신경쓸 게 많아진다.
    */
    private void changeTeam(Team team){
        if (this.team != null) { // 기존에 이미 팀이 존재한다면
            this.team.getMembers().remove(this); // 관계를 끊는다.
        }

        this.team = team;
        team.getMembers().add(this);
    }

    public Member() { // protected 이상의 기본 생성자는 필수, 내부적으로 Reflection을 쓰서 동적으로 객체를 생성해내야 하기 때문에
    }

    public Member(long id, String name) {
        this.id = id;
        this.userName = name;
    }
}
