package com.posts.post_platform.service.post;

import com.posts.post_platform.dto.PostDto;
import com.posts.post_platform.mapper.PostMapper;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.Post;
import com.posts.post_platform.model.Status;
import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.CommunityRepository;
import com.posts.post_platform.repository.PostRepository;
import com.posts.post_platform.repository.UserRepositories;
import com.posts.post_platform.requests.UpdatePostRequest;
import com.posts.post_platform.response.PostResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class PostServiceImpl implements PostService{

    private final CommunityRepository communityRepository;
    private final UserRepositories userRepositories;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public PostServiceImpl(CommunityRepository communityRepository,
                            UserRepositories userRepositories,
                            PostRepository postRepository,
                            PostMapper postMapper) {
        this.communityRepository = communityRepository;
        this.userRepositories = userRepositories;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
    }

    /**
     * This service manages all post-related operations including creating, retrieving, updating, and deleting posts.
     * - `createPost`: Creates a new post in the specified community by the user.
     * - `getPostById`: Retrieves a post by its ID.
     * - `getPostsByCommunity`: Retrieves all posts from a specific community based on its name, checking access levels.
     * - `getPostsByCommunityId`: Retrieves all posts from a community by its ID, checking user access.
     * - `getPostsByUsername`: Retrieves all posts created by a specific user, filtered by access level (public/private).
     * - `updatePost`: Updates the post if the current user is the creator of the post, ensuring proper handling of public and private posts.
     * - `deletePost`: Soft-deletes the post by setting its content and title to null and changing the status to DELETED.
     */

    @Override
    @Transactional
    public PostResponse createPost(Community community, PostDto postDto, User user) {
       Post post = postRepository.save(postMapper.convertPostDtoToPostModel(postDto, Status.ACTIVE.name(), user, community));
       return postMapper.convertPostToPostResponse(post);
    }

    @Override
    public PostResponse getPostById(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) return postMapper.convertPostToPostResponse(optionalPost.get());
        throw new IllegalArgumentException("Post not found with id : " + postId);
    }

    @Override
    public List<PostResponse> getPostsByCommunity(String communityName, String username) {
        Optional<Community> optionalCommunity = communityRepository.findByCommunityName(communityName);
        Optional<User> optionalUser = userRepositories.findUserByUsername(username);
        if (optionalCommunity.isPresent()) {
            Community community = optionalCommunity.get();
            if(optionalUser.isPresent()) {
                // first we need to check community whether it is private or not
                if (community.getAccess_level().name().equalsIgnoreCase("private")) {
                    boolean isMember = community.getApprovedUsers().stream().anyMatch(member -> member.getUsername().equals(username));
                    boolean isModerator = community.getModerators().stream().anyMatch(moderator -> moderator.getUsername().equals(username));
                    if (isMember || isModerator) {
                        List<Post> posts = community.getPosts();
                        return posts.stream().map(postMapper::convertPostToPostResponse).toList();
                    } else {
                        throw new IllegalArgumentException("You are not a member or moderator");
                    }
                } else {
                    List<Post> posts = community.getPosts();
                    return posts.stream().map(postMapper::convertPostToPostResponse).toList();
                }
            } else {
                throw new IllegalArgumentException("User not found with username : " + username);
            }
        } else {
            throw new IllegalArgumentException("Community not found with community name " + communityName);
        }
    }

    @Override
    public List<PostResponse> getPostsByCommunityId(Long communityId, String username) {
        Optional<Community> optionalCommunity = communityRepository.findById(communityId);
        Optional<User> optionalUser = userRepositories.findUserByUsername(username);
        if (optionalCommunity.isPresent()) {
            Community community = optionalCommunity.get();
            if (optionalUser.isPresent()) {
                if (community.getAccess_level().name().equalsIgnoreCase("private")) {
                    boolean isMember = community.getApprovedUsers().stream().anyMatch(member -> member.getUsername().equals(username));
                    boolean isModerator = community.getModerators().stream().anyMatch(mod -> mod.getUsername().equals(username));
                    if (isMember || isModerator) {
                        List<Post> posts = postRepository.findAllPostsByCommunityId(communityId);
                        return posts.stream().map(postMapper::convertPostToPostResponse).toList();
                    } else {
                        throw new IllegalArgumentException("You can not access posts, you are not a member or moderator");
                    }
                } else {
                    List<Post> posts = postRepository.findAllPostsByCommunityId(communityId);
                    return posts.stream().map(postMapper::convertPostToPostResponse).toList();
                }
            }
            throw new IllegalArgumentException("User not found with username : " + username);
        }
        throw new IllegalArgumentException("Community not found with community id : " + communityId);
    }

    @Override
    public List<PostResponse> getPostsByUsername(String username, String u_name) {
        // u_name is going to be person that logged in.
        Optional<User> optionalUser = userRepositories.findUserByUsername(username);
        if (optionalUser.isPresent()) {
            List<Post> posts = optionalUser.get().getPosts();
            List<Post> filteredPublicPosts = posts.stream()
                    .filter(post -> post.getCommunity().getAccess_level().name().equalsIgnoreCase("public")).toList();
            if (u_name != null) {
                List<Post> filteredPrivatePosts = posts.stream()
                        .filter(post -> post.getCommunity().getAccess_level().name().equalsIgnoreCase("private")
                                && post.getCommunity().isAccess(u_name) && post.getStatus().name().equalsIgnoreCase("active")).toList();
                List<Post> combinedPosts = Stream.concat(filteredPrivatePosts.stream(), filteredPublicPosts.stream())
                        .toList();
                return combinedPosts.stream().map(postMapper::convertPostToPostResponse).toList();
            }
            return filteredPublicPosts.stream().map(postMapper::convertPostToPostResponse).toList();
        }
        throw new IllegalArgumentException("User not found with username : " + username);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest updatePostRequest, String username) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (post.getCreator().getUsername().equals(username)) {
                Post updatedPost;
                if (post.getCommunity().getAccess_level().name().equalsIgnoreCase("public")) {
                    updatedPost = postRepository.save(postMapper.updatePost(updatePostRequest, post));
                } else {
                    updatedPost = postRepository.save(postMapper.copyPostForUpdated(updatePostRequest, post));
                    post.setStatus(Status.INACTIVE);
                    postRepository.save(post);
                }
                return postMapper.convertPostToPostResponse(updatedPost);
            }
            throw new IllegalArgumentException("Username not match with post creator");
        }
        throw new IllegalArgumentException("Post not found with id : " + postId);
    }

    @Override
    public PostResponse deletePost(Long postId, String username) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (post.getCreator().getUsername().equals(username)) {
                post.setContent(null);
                post.setTitle(null);
                post.setStatus(Status.DELETED);
                postRepository.save(post);
                return postMapper.convertPostToPostResponse(post);
            }
        }
        throw new IllegalArgumentException("Post not found with id : " + postId);
    }

}
