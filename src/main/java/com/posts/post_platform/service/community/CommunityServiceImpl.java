package com.posts.post_platform.service.community;

import com.posts.post_platform.dto.UserDto;
import com.posts.post_platform.exceptions.CommunityAlreadyExistsException;
import com.posts.post_platform.exceptions.CommunityNotFoundException;
import com.posts.post_platform.exceptions.UnauthorizedActionException;
import com.posts.post_platform.exceptions.UserNotFoundException;
import com.posts.post_platform.mapper.CommunityMapper;
import com.posts.post_platform.model.AccessLevel;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.CommunityRepository;
import com.posts.post_platform.repository.UserRepositories;
import com.posts.post_platform.requests.CommunityRequest;
import com.posts.post_platform.requests.UpdateCommunityRequest;
import com.posts.post_platform.response.CommunityResponse;
import com.posts.post_platform.response.CommunityResponseWithApprovedUsers;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommunityServiceImpl implements CommunityService{
    private final CommunityRepository communityRepository;
    private final CommunityMapper communityMapper;
    private final UserRepositories userRepositories;

    @Autowired
    public CommunityServiceImpl(CommunityRepository communityRepository, CommunityMapper communityMapper, UserRepositories userRepositories) {
        this.communityRepository = communityRepository;
        this.communityMapper = communityMapper;
        this.userRepositories = userRepositories;
    }

    /**
     * Creates a new community if one doesn't already exist with the provided community name.
     * The new community is associated with the user who is creating it.
     *
     * @param communityRequest the request object containing the community details
     * @param username the username of the user creating the community
     * @return the response object containing the created community details
     * @throws UserNotFoundException if the user with the specified username is not found
     * @throws CommunityAlreadyExistsException if a community already exists with the specified name
     */
    @Override
    @Transactional
    public CommunityResponse createCommunity(CommunityRequest communityRequest, String username) {
        Optional<Community> optionalCommunity = communityRepository.findByCommunityName(communityRequest.getCommunity_name());
        User user = userRepositories.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        if (optionalCommunity.isPresent()) throw new CommunityAlreadyExistsException("Community already exists with community name : " + communityRequest.getCommunity_name());
        Community community = communityRepository.save(communityMapper.convertCommunityRequestToModel(communityRequest, user));
        user.getCreated_communities().add(community);
        userRepositories.save(user);
        return communityMapper.convertCommunityToResponse(community);
    }

    /**
     * Retrieves the details of a community by its ID.
     *
     * @param communityId the ID of the community to retrieve
     * @return the response object containing the community details
     * @throws EntityNotFoundException if the community with the specified ID is not found
     */
    @Override
    public CommunityResponse getCommunity(Long communityId) {
        Optional<Community> optionalCommunity = communityRepository.findById(communityId);
        if(optionalCommunity.isPresent()) {
            return communityMapper.convertCommunityToResponse(optionalCommunity.get());
        }
        throw new EntityNotFoundException("Community entity with id : " + communityId + " not found");
    }

    /**
     * Retrieves the details of a community by its name.
     *
     * @param communityName the name of the community to retrieve
     * @return the response object containing the community details
     * @throws EntityNotFoundException if the community with the specified name is not found
     */
    @Override
    public CommunityResponse getCommunityByName(String communityName) {
        Optional<Community> optionalCommunity = communityRepository.findByCommunityName(communityName);
        if(optionalCommunity.isPresent()) {
            return communityMapper.convertCommunityToResponse(optionalCommunity.get());
        }
        throw new EntityNotFoundException("Community entity with name : " + communityName + " not found");
    }

    /**
     * Retrieves all communities in the system.
     *
     * @return a list of response objects containing the details of all communities
     * @throws EntityNotFoundException if no communities are found
     */
    @Override
    public List<CommunityResponse> getAllCommunities() {
        List<Community> communities = communityRepository.findAll();
        if (!communities.isEmpty()) {
            return communities.stream().map(communityMapper::convertCommunityToResponse).toList();
        }
        throw new EntityNotFoundException("There is no community");
    }

    /**
     * Updates the details of an existing community. The user must be either the creator or a moderator of the community to update it.
     *
     * @param communityName the name of the community to update
     * @param updateCommunityRequest the request object containing the updated community details
     * @param username the username of the user requesting the update
     * @return the response object containing the updated community details
     * @throws CommunityNotFoundException if the community with the specified name is not found
     * @throws UserNotFoundException if the user with the specified username is not found
     * @throws UnauthorizedActionException if the user does not have permission to update the community
     */
    @Override
    @Transactional
    public CommunityResponse updateCommunity(String communityName, UpdateCommunityRequest updateCommunityRequest, String username) {
        Community community = communityRepository.findByCommunityName(communityName).orElseThrow(() -> new CommunityNotFoundException("Community not found with community name : " + communityName));
        User user = userRepositories.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        boolean isModerator = community.getModerators().stream().anyMatch(moderator -> moderator.getUsername().equals(username));
        boolean isCreator = community.getCreator().equals(user);
        // user can access to update community
        if (isModerator || isCreator) {
            if (updateCommunityRequest.getCommunity_name() != null && communityRepository.findByCommunityName(updateCommunityRequest.getCommunity_name()).isEmpty()) {
                community.setCommunityName(updateCommunityRequest.getCommunity_name());
            }
            if (updateCommunityRequest.getDescription() != null) {
                community.setDescription(updateCommunityRequest.getDescription());
            }
            if (updateCommunityRequest.getTopics() != null) {
                community.setTopics(updateCommunityRequest.getTopics());
            }
            if (updateCommunityRequest.getAccess_level() != null) {
                community.setAccess_level(AccessLevel.fromString(updateCommunityRequest.getAccess_level()));
            }
            community.setUpdatedAt(LocalDateTime.now());
            return communityMapper.convertCommunityToResponse(communityRepository.save(community));
        }
        throw new UnauthorizedActionException("Unauthorized action");
    }

    /**
     * Retrieves all members of a community.
     *
     * @param communityId the ID of the community whose members are to be retrieved
     * @return a list of UserDto objects representing the members of the community
     * @throws IllegalArgumentException if the community with the specified ID is not found
     */
    @Override
    public List<UserDto> getAllMembers(Long communityId) {
        Optional<Community> optionalCommunity = communityRepository.findById(communityId);
        if(optionalCommunity.isPresent()) {
            List<User> userList = communityRepository.getAllMembersByUsingCommunityId(communityId);
            return userList.stream().map(user -> UserDto.builder().email(user.getEmail()).username(user.getUsername())
                    .createdAt(user.getCreatedAt())
                    .role(user.getRole())
                    .build()).toList();
        }
        throw new IllegalArgumentException("Community not found with id : " + communityId);
    }

    /**
     * Retrieves the count of approved members in a community.
     *
     * @param communityId the ID of the community whose members count is to be retrieved
     * @return the number of approved members in the community
     * @throws IllegalArgumentException if the community with the specified ID is not found
     */
    @Override
    public int getMembersCount(Long communityId) {
        Optional<Community> optionalCommunity = communityRepository.findById(communityId);
        if(optionalCommunity.isPresent()) {
            return communityRepository.countApprovedUsersByCommunityId(communityId);
        }
        throw new IllegalArgumentException("Community not found with id : " + communityId);
    }

    /**
     * Finds a community by its ID and returns it as an Optional.
     *
     * @param communityId the ID of the community to retrieve
     * @return an Optional containing the community if found, or an empty Optional if not found
     */
    @Override
    public Optional<Community> findCommunityById(Long communityId) {
        return communityRepository.findById(communityId);
    }

    /**
     * Adds a user to a community, provided they are not already a member.
     *
     * @param community the community to which the user is to be added
     * @param username the username of the user to add
     * @return the response object containing the updated community details with the approved users
     * @throws UserNotFoundException if the user with the specified username is not found
     * @throws IllegalArgumentException if the user is already a member of the community
     */
    @Override
    @Transactional
    public CommunityResponseWithApprovedUsers addUserToCommunity(Community community, String username) {
        User user = userRepositories.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        if (communityRepository.countUserInCommunity(community.getId(), user.getId()) == 0) {
            if (community.getApprovedUsers() == null) {
                community.setApprovedUsers(new ArrayList<>());
            }
            community.getApprovedUsers().add(user);
            communityRepository.save(community);
            return communityMapper.convertCommunityToResponseWithApprovedUsers(community);
        }
       throw new IllegalArgumentException("User already is member of that community");
    }

    /**
     * Checks if a user is the creator of a specific community.
     *
     * @param communityId the ID of the community to check
     * @param username the username of the user to check
     * @return true if the user is the creator, false otherwise
     */
    @Override
    public boolean isCreator(Long communityId, String username) {
        return communityRepository.isUserCreator(communityId, username);
    }

    /**
     * Checks if a user is a member of a specific community.
     *
     * @param communityName the name of the community to check
     * @param userId the ID of the user to check
     * @return true if the user is a member, false otherwise
     */
    @Override
    public boolean isMember(String communityName, Long userId) {
        Long result = communityRepository.isUserMember(communityName, userId);
        return result == 1;
    }

    /**
     * Checks if a user is a moderator of a specific community.
     *
     * @param communityId the ID of the community to check
     * @param username the username of the user to check
     * @return true if the user is a moderator, false otherwise
     */
    @Override
    public boolean isModerator(Long communityId, String username) {
        return communityRepository.isUserModerator(communityId, username);
    }

    /**
     * Checks if a community is private.
     *
     * @param communityName the name of the community to check
     * @return true if the community is private, false otherwise
     */
    @Override
    public boolean isCommunityPrivate(String communityName) {
        return communityRepository.isCommunityPrivate(communityName) == 1;
    }
}
