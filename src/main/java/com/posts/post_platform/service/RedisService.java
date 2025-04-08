package com.posts.post_platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posts.post_platform.dto.PostDto;
import com.posts.post_platform.model.RequestStatus;
import com.posts.post_platform.model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisService {
    private final JedisPooled jedis;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisService(JedisPooled jedis) {
        this.jedis = jedis;
    }

    /**
     * This method saves a join request for a community. If the user has already requested to join,
     * it returns false. Otherwise, it appends the join request to the list of pending requests in Redis.
     *
     * @param communityId The ID of the community the user is requesting to join.
     * @param userId The ID of the user making the join request.
     * @param username The username of the user making the join request.
     * @return true if the join request is saved successfully, false if the user has already requested to join.
     */
    public boolean saveJoinRequest(Long communityId, Long userId, String username) {
        String key = "community_id:" + communityId;
        String jsonPath = "$";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_id", userId);
        jsonObject.put("username", username);
        jsonObject.put("request_timestamp", System.currentTimeMillis());

        if (!jedis.exists(key)) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            jedis.jsonSet(key, Path2.of(jsonPath), jsonArray);
            return true;
        }

        Object jsonArray = jedis.jsonGet(key);
        List<?> objects = (List<?>) jsonArray;
        for (Object object : objects) {
            Map<?, ?> map = (Map<?, ?>) object;
            if (((Number) map.get("user_id")).longValue() == userId) {
                return false;
            }
        }

        jedis.jsonArrAppend(key, Path2.of(jsonPath), jsonObject);
        return true;
    }

    /**
     * This method retrieves the list of all pending join requests for a specific community from Redis.
     *
     * @param communityId The ID of the community whose join requests are to be fetched.
     * @return A list of maps representing pending join requests for the given community.
     */
    public List<Map<String, Object>> getPendingJoinRequests(Long communityId) {
        String key = "community_id:" + communityId;
        return (List<Map<String, Object>>) jedis.jsonGet(key);
    }

    /**
     * This method searches for the index of a specific user in the list of pending join requests for a community.
     *
     * @param communityId The ID of the community.
     * @param userId The ID of the user whose request index is to be found.
     * @return The index of the user’s join request, or -1 if the user’s request is not found.
     */
    public int findIndex(Long communityId, Long userId) {
        List<Map<String, Object>> list = this.getPendingJoinRequests(communityId);
        int index = -1;

        for (int i = 0; i < list.size(); i++) {
            Object userIdFromList = list.get(i).get("user_id");

            if (userIdFromList instanceof Number) {
                Long currentUserId = ((Number) userIdFromList).longValue();
                if (currentUserId.equals(userId)) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    /**
     * This method searches for the index of a specific post in the list of all pending posts for a community.
     *
     * @param communityId The ID of the community.
     * @param postId The ID of the post whose index is to be found.
     * @return The index of the post, or -1 if the post is not found.
     */
    public int findIndexForPost(Long communityId, String postId) {
        List<Map<String, Object>> posts = this.getAllPendingPostsFromRedis(communityId);
        int index = -1;

        for (int i = 0; i < posts.size(); i++) {
            Object postIdFromPosts = posts.get(i).get("post_id");
            if (postIdFromPosts instanceof String && postIdFromPosts.equals(postId)) {
                index = i;
                break;
            }
        }

        return index;
    }

    /**
     * This method deletes a value from Redis based on a specific key and index.
     *
     * @param preKey The prefix of the key (e.g., "community_id:").
     * @param communityId The ID of the community.
     * @param index The index of the value to be deleted.
     * @return The number of items removed from the Redis list.
     */
    public long deleteValueFromRedis(String preKey, Long communityId, Integer index) {
        String key = preKey + communityId;
        String jsonPath = String.format("$[%d]", index);

        return jedis.jsonDel(key, Path2.of(jsonPath));
    }

    /**
     * This method saves a pending post for a community in Redis. The post is stored with information about
     * the post creator, title, content, and status (pending).
     *
     * @param communityId The ID of the community where the post is being created.
     * @param postDto The post details (title, content, etc.).
     * @param user The user who is creating the post.
     * @return A JSON object representing the saved post.
     */
    public JSONObject savePendingPost(Long communityId, PostDto postDto, User user) {
        // post:community_id:{communityId}
        String key = "post:community_id:" + communityId;
        String jsonPath = "$";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("post_id", UUID.randomUUID());
        jsonObject.put("creator_id", user.getId());
        jsonObject.put("creator_username", user.getUsername());
        jsonObject.put("title", postDto.getTitle());
        jsonObject.put("content", postDto.getContent());
        jsonObject.put("requested_at", LocalDateTime.now());
        jsonObject.put("status", RequestStatus.PENDING);

        if (!jedis.exists(key)) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            jedis.jsonSet(key, Path2.of(jsonPath), jsonArray);
        } else {
            jedis.jsonArrAppend(key, Path2.of(jsonPath), jsonObject);
        }
        return jsonObject;
    }

    /**
     * This method retrieves all pending posts for a community from Redis.
     *
     * @param communityId The ID of the community whose posts are to be fetched.
     * @return A list of maps representing all pending posts for the given community.
     */
    public List<Map<String, Object>> getAllPendingPostsFromRedis(Long communityId) {
        String key = "post:community_id:" + communityId;
        if (jedis.exists(key)) return (List<Map<String, Object>>) jedis.jsonGet(key);
        return Collections.emptyList();
    }
}
