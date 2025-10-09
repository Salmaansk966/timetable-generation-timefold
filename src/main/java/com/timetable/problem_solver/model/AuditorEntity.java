package com.timetable.problem_solver.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditorEntity {

    private static final String SYSTEM = "System";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @PrePersist
    public void beforePersist(){
        UserDTO userDTO = UserContextHolder.getUserDto();
        if(Objects.isNull(userDTO)){
            this.setCreatedBy(SYSTEM);
            this.setUpdatedBy(SYSTEM);
            return;
        }
        this.setCreatedBy(userDTO.getFullName());
        this.setUpdatedBy(userDTO.getFullName());
    }

    @PreUpdate
    public void beforeUpdate(){
        UserDTO userDTO = UserContextHolder.getUserDto();
        if(Objects.isNull(userDTO)){
            this.setUpdatedBy(SYSTEM);
            return;
        }
        this.setUpdatedBy(userDTO.getFullName());
    }
}
