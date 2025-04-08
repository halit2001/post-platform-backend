package com.posts.post_platform.repository;

import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findByCommunityName(String communityName);

    @Query("SELECT COUNT(u) FROM Community c JOIN c.approvedUsers u WHERE c.id = :communityId")
    int countApprovedUsersByCommunityId(@Param("communityId") Long communityId);

    @Query("SELECT c.approvedUsers FROM Community c WHERE c.id = :communityId")
    List<User> getAllMembersByUsingCommunityId(@Param("communityId") Long communityId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM Community c JOIN c.moderators u WHERE c.id = :communityId AND u.username = :username ")
    boolean isUserModerator(@Param("communityId") Long communityId, @Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM Community c JOIN c.creator u WHERE c.id =:communityId AND u.username = :username")
    boolean isUserCreator(@Param("communityId") Long communityId, @Param("username") String username);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END " +
            "FROM community_users cu " +
            "JOIN communities c ON cu.community_id = c.id " +
            "JOIN users u ON cu.user_id = u.id " +
            "WHERE c.community_name = :communityName AND u.id = :userId", nativeQuery = true)
    Long isUserMember(@Param("communityName") String communityName, @Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM community_users WHERE community_id = :communityId AND user_id = :userId", nativeQuery = true)
    int countUserInCommunity(@Param("communityId") Long communityId, @Param("userId") Long userId);

    @Query(value = "select case when COUNT(*) > 0 THEN TRUE ELSE FALSE END " +
            "from communities c " +
            "where c.access_level = '1' and c.community_name = :communityName ", nativeQuery = true)
    Long isCommunityPrivate(@Param("communityName") String communityName);

}
