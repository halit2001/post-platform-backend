package com.posts.post_platform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String email;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private List<Role> role;

    @ManyToMany(mappedBy = "approvedUsers")
    private List<Community> communities_attended;

    @OneToMany(mappedBy = "creator")
    private List<Community> created_communities = new ArrayList<>();

    @ManyToMany(mappedBy = "moderators")
    private List<Community> moderatedCommunities;

    @OneToMany(mappedBy = "creator")
    private List<Post> posts;

    @OneToMany(mappedBy = "commentAuthor")
    private List<Comment> comments;
}
