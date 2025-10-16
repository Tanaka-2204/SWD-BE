package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "kb_chunk")
public class KbChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chunk_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private KbSource source;

    @NotNull
    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "embedding")
    private String embedding;

    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

}