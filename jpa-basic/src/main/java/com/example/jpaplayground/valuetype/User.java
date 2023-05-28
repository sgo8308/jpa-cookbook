package com.example.jpaplayground.valuetype;


import java.util.ArrayList;
import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * #jpabasic/valuetype 값 타입 설명
     *
     * 값 타입의 경우 실제 테이블에는 모든 필드를 똑같이 같고 있지만, 애플리케이션 상에서 논리적으로 값들을 묶어서
     * 하나의 값으로 표현하고 싶을 때 사용할 수 있다. 다른 일반적인 값들과 마찬가지로 엔티티의 생명 주기에 의존한다.
     * 관련 메소드를 값 타입에 넣을 수 있어 응집도가 올라가고, 재사용하기 좋다.
     *
     * 위에 있는 id와 똑같은 라이프 사이클을 갖는다. User 엔티티가 저장될 때 같이 저장되고 없어질 때 같이 없어진다.
     *
     * 반드시 불변 객체로 만들어서 변경이 불가능하도록 만들어야 값 객체 공유로 인한 사이드 이펙트를 방지할 수 있다.
     */
    @Embedded()
    @AttributeOverrides({ // 만약 동일한 값 타입을 사용하는 다른 변수(workAddress)가 있을 경우 사용
            @AttributeOverride(name = "city", column = @Column(name = "home_city")),
            @AttributeOverride(name = "street", column = @Column(name = "home_street")),
            @AttributeOverride(name = "zipcode", column = @Column(name = "home_zipcode"))
    })
    private Address homeAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "work_city")),
            @AttributeOverride(name = "street", column = @Column(name = "work_street")),
            @AttributeOverride(name = "zipcode", column = @Column(name = "work_zipcode"))
    })
    private Address workAddress;

    /**
     * #jpabasic/valuetype 값 타입을 컬렉션으로 쓸 경우 => 웬만하면 엔티티로 승격시켜 사용할 것

      이 경우에는 정규화를 위해 실제로 DB에 테이블이 새로 만들어져야 한다.
      이 때 컬렉션 안에 값 타입을 업데이트할 경우 JPA에서는 컬렉션에 모든 값을 DB에서 지우고 현재 존재하는 것을 다시 다 집어넣는다.
      왜냐하면 값 타입의 경우 식별자가 없어 추적이 불가능하기 때문이다. 삭제되거나 업데이트된 것을 골라내는 것이 불가능하다.

      따라서 값 타입을 컬렉션으로 쓸 경우 Entity로 상승시키자.
      그리고 값 타입이 다른 엔티티에서도 쓰이면 AddressEntity처럼 값 타입을 래핑해서 내부에 갖게끔 구성하는 것이 좋다.
      값 타입이 다른 데서 안 쓰이면 값 타입의 프로퍼티를 상승시킨 엔티티로 옮겨준다.

      그 후 아래와 같이 cascade, orphanremoval 옵션을 맞춰주면 거의 값 타입 컬렉션처럼 생명주기를 엔티티와 같이 가져갈 수 있다.
      이렇게 하는 것이 실무에서 쿼리 최적화하기에도 유리하다.
    */
    @OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<AddressEntity> addressHistory = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public Address getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(Address workAddress) {
        this.workAddress = workAddress;
    }

    public List<AddressEntity> getAddressHistory() {
        return addressHistory;
    }

    public void setAddressHistory(
            List<AddressEntity> addressHistory) {
        this.addressHistory = addressHistory;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

}
