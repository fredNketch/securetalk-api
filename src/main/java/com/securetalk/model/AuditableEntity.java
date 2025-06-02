package com.securetalk.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entité de base avec support de l'audit
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_ip", length = 45, updatable = false)
    private String createdIp;

    @Column(name = "modified_ip", length = 45)
    private String modifiedIp;
}
