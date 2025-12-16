package com.finacial.wealth.backoffice.auth.service;

import jakarta.transaction.Transactional;

import com.finacial.wealth.backoffice.auth.entity.BoAdminRole;
import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.repo.BoAdminRoleRepository;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/*@Component
@RequiredArgsConstructor
public class AdminSeedRunner implements CommandLineRunner {

  private final BoAdminUserRepository userRepo;
  private final BackofficeAuthService authService;

  @Override
  @Transactional
  public void run(String... args) {
    userRepo.findByEmailIgnoreCase("superadmin@finacialwealth.com").ifPresentOrElse(u -> {}, () -> {
      BoAdminRole role = BoAdminRole.builder().id(1L).name("SUPER_ADMIN").build();
      BoAdminUser u = BoAdminUser.builder()
          .email("superadmin@finacialwealth.com")
          .fullName("Super Admin")
          .passwordHash(authService.hashPassword("Password123!"))
          .status(BoAdminUser.Status.ACTIVE)
          .mfaEnabled(false)
          .roles(Set.of(role))
          .build();
      userRepo.save(u);
    });
  }
}*/

@Component
@RequiredArgsConstructor
public class AdminSeedRunner implements ApplicationRunner {

    private final BoAdminRoleRepository roleRepo;
    private final BoAdminUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // 1. Ensure roles exist FIRST
        BoAdminRole superAdminRole = roleRepo
                .findByName("SUPER_ADMIN")
                .orElseGet(() -> roleRepo.saveAndFlush(
                        new BoAdminRole(null, "SUPER_ADMIN")
                ));

        // 2. Ensure admin user exists
        BoAdminUser admin = userRepo
                .findByEmail("superadmin@finacialwealth.com")
                .orElseGet(() -> {
                    BoAdminUser u = new BoAdminUser();
                    u.setEmail("superadmin@finacialwealth.com");
                    //u.setPassword(passwordEncoder.encode("Password123!"));
                    u.setPasswordHash(passwordEncoder.encode("Password123!"));
                    u.setFullName("Super Admin");              // ✅ add this
                    u.setEnabled(true);
                    return userRepo.save(u);
                });

        // 3. Attach role ONLY after both exist
        if (!admin.getRoles().contains(superAdminRole)) {
            admin.getRoles().add(superAdminRole);
            userRepo.saveAndFlush(admin);
        }
    }
}

