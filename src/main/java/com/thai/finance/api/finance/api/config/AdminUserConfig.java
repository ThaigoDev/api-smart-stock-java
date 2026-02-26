package com.thai.finance.api.finance.api.config;

import com.thai.finance.api.finance.api.domain.entities.Role;
import com.thai.finance.api.finance.api.domain.entities.User;
import com.thai.finance.api.finance.api.respository.RoleRepository;
import com.thai.finance.api.finance.api.respository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
public class AdminUserConfig  implements CommandLineRunner {
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    public AdminUserConfig(RoleRepository roleRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String ...args) throws  Exception {
        var RoleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());
        var userAdmin  = userRepository.findByEmail("admin@admin.com");
      userAdmin.ifPresentOrElse((user)-> System.out.println("Admin exist."), ()-> {
          var user = new User();
          user.setUsername("admin");
          user.setEmail("admin@admin.com");
          user.setPassword( passwordEncoder.encode("admin"));
          user.setRoles(Set.of(RoleAdmin));

          userRepository.save(user);

      });
    }
}
