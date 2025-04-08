package com.posts.post_platform.repository;

import com.posts.post_platform.model.Comment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = "select * from comments cu where cu.id In (Select co.id " +
            "from posts p " +
            "join comments co on co.post_id = p.id " +
            "join communities c on c.id = p.community_id " +
            "where p.id = :postId and c.community_name = :communityName and co.parent_id IS NULL)", nativeQuery = true)
    List<Comment> getAllParentCommentsFromPost(@Param("communityName") String communityName, @Param("postId") Long postId);


    @Query(value = "select * from comments cu where cu.id In (Select cu.id as comment_id " +
            "from posts p " +
            "join comments co on co.post_id = p.id " +
            "join communities c on c.id = p.community_id " +
            "where p.id = :postId and c.community_name = :communityName and co.parent_id = :parentId)", nativeQuery = true)
    List<Comment> getAllChildCommentsFromParentId(@Param("parentId") Long parentId, @Param("communityName") String communityName, @Param("postId") Long postId);

    @Query(value = "select * from post_database.comments " +
            "where post_id = :postId and parent_id IS NULL " +
            "order by created_at desc ", nativeQuery = true)
    List<Comment> getAllParentCommentsFromPostSortedByOld(@Param("postId") Long postId);

    @Query(value = "select * from post_database.comments c " +
            "where c.post_id = :postId and c.parent_id IS NULL " +
            "order by c.like desc", nativeQuery = true)
    List<Comment> getAllParentCommentsFromPostSortedByTop(@Param("postId") Long postId);

    @Query("select c from Comment c where c.id = :commentId and c.post.id = :postId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Comment> findCommentByIdWithLock(@Param("commentId") Long commentId, @Param("postId") Long postId);


}
