package com.gigforce.crm.contact;

import com.gigforce.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contacts")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Contact extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String company;

    @Column(length = 1000)
    private String notes;
}
