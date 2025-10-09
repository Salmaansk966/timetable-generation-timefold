package com.timetable.problem_solver.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "role")
@Entity
@Setter
@Getter
public class Role extends AuditorEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", length = 80, nullable = false, unique = true)
    private String roleName;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private Boolean isActive;

    @Column(name = "created_by", length = 120, nullable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 120, nullable = false)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permission_map", joinColumns = @JoinColumn(name = "role_id_fk"),
            inverseJoinColumns = @JoinColumn(name = "permission_id_fk"))
    private List<Permission> permissions = new ArrayList<>();

    public void addPermission(Permission permission){
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission){
        this.permissions.remove(permission);
    }


}
