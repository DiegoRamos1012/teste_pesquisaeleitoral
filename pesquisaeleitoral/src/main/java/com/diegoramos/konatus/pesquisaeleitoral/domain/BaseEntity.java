package com.diegoramos.konatus.pesquisaeleitoral.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BaseEntity é a classe base para todas as entidades do sistema.
 * Contém campos comuns como id, name e auditoria (createdAt, lastTimeChanged).
 */

@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 250)
    String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(nullable = false)
    private LocalDateTime lastTimeChanged;

    /**
     * Executa automaticamente antes da entidade ser salva no banco (INSERT)
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastTimeChanged = createdAt;
    }

    /**
     * Executa automaticamente antes da entidade existente receber uma atualização no banco (UPDATE)
     */
    @PreUpdate
    protected void onUpdate() {
        lastTimeChanged = LocalDateTime.now();
    }

    protected void updateLastTimeChanged() {
        this.lastTimeChanged = LocalDateTime.now();
    }
}