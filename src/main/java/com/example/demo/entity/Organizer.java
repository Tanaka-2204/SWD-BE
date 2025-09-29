package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "organizer")
public class Organizer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "organizer_id")
    private UUID organizerId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "contact_info", columnDefinition = "TEXT")
    private String contactInfo;

}