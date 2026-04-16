package com.finacial.wealth.backoffice.auth.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "bo_admin_role")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BoAdminRole {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String name;

  @Builder.Default
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
          name = "bo_role_permission",
          joinColumns = @JoinColumn(name = "role_id"),
          inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  private Set<BoPermission> permissions = new HashSet<>();

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Set<BoPermission> getPermissions() {
    return permissions;
  }
}
