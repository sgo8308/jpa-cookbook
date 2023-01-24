package com.example.jpaplayground.jpql;

import com.example.jpaplayground.valuetype.Address;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.BatchSize;

@Entity
/**
 * #jpabasic NamedQuery - 미리 정의해서 이름을 부여해두고 사용하는 JPQL
 *
  정적 쿼리(동적 쿼리 불가능)이며 어노테이션이나 XML을 통해 사용 가능
  애플리케이션 로딩 시점에 초기화하고 재사용한다.
  애플리케이션 로딩 시점에 쿼리를 검증해주기 때문에 잘못된 쿼리는 빨리 알아낼 수 있음

  * Spring Data JPA에서 메서드 위에 @Query("select h from Human h where h.name = :name")
    이런 식으로 사용할 수 있게 해주는데, 이것이 내부적으로 NamedQuery를 사용한 것이다.

  @see com.example.jpaplayground.JPQLTest#namedQuery()
*/
@NamedQuery(
        name = "Human.findByName",
        query="select h from Human h where h.name = :name")
public class Human {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;

    @Embedded
    private Address address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @BatchSize(size = 5) // application.yml에 default_batch_fetch_size: 5으로 글로벌 적용도 가능하고, 글로벌이 더 많이 쓰인다.
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "human")
    private List<Mentor> mentors = new ArrayList<>();


    public Room getRoom() {
        return room;
    }

    public List<Mentor> getMentors() {
        return mentors;
    }

    public void setMentors(List<Mentor> mentors) {
        this.mentors = mentors;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Address getAddress() {
        return address;
    }

    public Room getGroup() {
        return room;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
