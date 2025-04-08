package com.posts.post_platform.repository;

import com.posts.post_platform.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select * from post_database.posts p " +
            "where p.community_id = :community_id", nativeQuery = true)
    List<Post> findAllPostsByCommunityId(@Param("community_id") Long community_id);

    @Query("SELECT p FROM Post p WHERE p.id =: post_id AND p.creator.id =: creator_id")
    Optional<Post> findPostByIdAndCreatorId(@Param("post_id") Long post_id, @Param("creator_id") Long creator_id);
}
