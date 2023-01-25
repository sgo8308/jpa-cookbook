package com.example.jpaplayground.basic;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Like {

    @Id @GeneratedValue
    private Long id;

    private int count;

}
