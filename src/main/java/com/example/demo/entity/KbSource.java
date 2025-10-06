package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "kb_source")
public class KbSource {
    @Id
    @ColumnDefault("nextval('kb_source_source_id_seq')")
    @Column(name = "source_id", nullable = false)
    private Long id;

    @Size(max = 200)
    @Column(name = "name", length = 200)
    private String name;

    @Size(max = 50)
    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "url", length = Integer.MAX_VALUE)
    private String url;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

}