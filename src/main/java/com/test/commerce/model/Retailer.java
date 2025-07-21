package com.test.commerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name="retailers")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Retailer extends User{
    private String retailerName;

    private String retailerAddress;
    @OneToMany(mappedBy = "retailer",fetch = FetchType.LAZY)
    private List<Product> Products;

}
