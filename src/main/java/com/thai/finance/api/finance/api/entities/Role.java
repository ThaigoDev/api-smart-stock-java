package com.thai.finance.api.finance.api.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;
    private String name;

    public enum Values {
        BASIC(1L),
        ADMIN (2L);
        long roleId;
        Values(Long roleId) {
            this.roleId = roleId;
        }
        public long getRoleId() {
            return roleId;
        }
    }

}
