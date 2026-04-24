package com.bali.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "village_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VillageGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @OneToMany(mappedBy = "villageGroup")
    private List<User> members;

    @OneToMany(mappedBy = "group")
    private List<Post> posts;
}
