package ru.skfu.softwarestore.entity;
import jakarta.persistence.*; import lombok.*; import java.util.UUID;
@Entity @Table(name="categories") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category { @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(nullable=false,unique=true,length=120) private String name; @Column(length=1000) private String description; }
