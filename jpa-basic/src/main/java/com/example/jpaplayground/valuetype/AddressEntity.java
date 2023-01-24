package com.example.jpaplayground.valuetype;

import java.util.Objects;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "adress_history")
public class AddressEntity {

    @Id @GeneratedValue
    private Long id;

    @Embedded
    private Address address;

    public AddressEntity(){}

    public AddressEntity(String city, String steet, String zipcode) {
        this.address = new Address(city, steet, zipcode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AddressEntity that = (AddressEntity) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address);
    }
}
