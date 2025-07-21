package com.test.commerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_seq")
    @SequenceGenerator(name = "cat_seq", sequenceName = "category_seq", allocationSize = 20)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;


    public Category(String name){
        this.name=name;
    }
}
