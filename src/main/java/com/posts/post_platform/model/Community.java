package com.posts.post_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "communities")
@NoArgsConstructor
@AllArgsConstructor
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "community_name")
    private String communityName;
    private String description;
    private List<String> topics;
    private AccessLevel access_level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "community_users", joinColumns = @JoinColumn(name = "community_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> approvedUsers;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "community_moderators", joinColumns = @JoinColumn(name = "community_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> moderators;

    @OneToMany(mappedBy = "community")
    private List<Post> posts;

    public boolean isAccess(String username) {
        return this.getApprovedUsers().stream().anyMatch(user -> user.getUsername().equals(username));
    }

    public boolean isPrivate() {
        return this.getAccess_level().name().equalsIgnoreCase("private");
    }
}
