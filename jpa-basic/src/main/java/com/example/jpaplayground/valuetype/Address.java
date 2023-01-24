package com.example.jpaplayground.valuetype;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Address {

    @Column
    private String city;
    private String street;
    private String zipcode;

    public Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getZipcode() {
        return zipcode;
    }

    /**
     * #jpabasic/valuetype 값 타입 equals 관련 주의 사항 - 필드 직접 접근 말고 getter 사용하기
     *
     * field에 직접 접근하면 프록시가 equals를 진행할 때 제대로 동작 안 할 수 있으므로 getter를 이용하게 하는 것이 좋다.
     * 예를 들어 프록시 대상으로 equals 메소드가 호출될 때 프록시는 Address를 상속하고 있으므로 부모의 equals 메소드를 실행하게 되고
     * field에 직접 접근하게 되면 실제 target 객체를 가져오지 않고 프록시의 field에 접근해서 사용하게 된다.

     * 출처 - https://inflearn.com/questions/553865
    */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address address = (Address) o;
        return Objects.equals(getCity(), address.getCity()) &&
                Objects.equals(getStreet(), address.getStreet()) &&
                Objects.equals(getZipcode(), address.getZipcode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCity(), getStreet(), getZipcode());
    }
}
