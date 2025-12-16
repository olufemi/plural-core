package com.finacial.wealth.backoffice.auth.entity;

import jakarta.persistence.*;
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
}
