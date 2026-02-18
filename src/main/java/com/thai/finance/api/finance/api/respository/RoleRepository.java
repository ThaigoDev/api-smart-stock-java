package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository <Role, Long>{
    Role findByName(String name);
}
