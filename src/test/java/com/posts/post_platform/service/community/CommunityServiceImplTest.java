package com.posts.post_platform.service.community;

import com.posts.post_platform.dto.UserDto;
import com.posts.post_platform.exceptions.CommunityAlreadyExistsException;
import com.posts.post_platform.exceptions.CommunityNotFoundException;
import com.posts.post_platform.exceptions.UnauthorizedActionException;
import com.posts.post_platform.exceptions.UserNotFoundException;
import com.posts.post_platform.mapper.CommunityMapper;
import com.posts.post_platform.model.AccessLevel;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.Role;
import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.CommunityRepository;
import com.posts.post_platform.repository.UserRepositories;
import com.posts.post_platform.requests.CommunityRequest;
import com.posts.post_platform.requests.UpdateCommunityRequest;
import com.posts.post_platform.response.CommunityResponse;
import com.posts.post_platform.response.CommunityResponseWithApprovedUsers;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private UserRepositories userRepository;

    @Mock
    private CommunityMapper communityMapper;

    @InjectMocks
    private CommunityServiceImpl communityService;

    private CommunityRequest communityRequest;
    private User mockUser;
    private Community mockCommunity;
    private CommunityResponse communityResponse;

    @BeforeEach
    void setUp() {
        communityRequest = new CommunityRequest();
        communityRequest.setCommunity_name("Tech Group");

        mockUser = new User();
        mockUser.setUsername("testUser");

        mockCommunity = new Community();
        mockCommunity.setCommunityName("Tech Group");
        mockCommunity.setId(1L);

        communityResponse = new CommunityResponse();
        communityResponse.setCommunity_name("Tech Group");
    }

    @Test
    void createCommunity_Success() {
        when(communityRepository.findByCommunityName("Tech Group")).thenReturn(Optional.empty());
        when(userRepository.findUserByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
        when(communityMapper.convertCommunityRequestToModel(communityRequest, mockUser)).thenReturn(mockCommunity);
        when(communityRepository.save(mockCommunity)).thenReturn(mockCommunity);
        when(communityMapper.convertCommunityToResponse(mockCommunity)).thenReturn(communityResponse);
        CommunityResponse response = communityService.createCommunity(communityRequest, mockUser.getUsername());

        assertNotNull(response);
        assertEquals("Tech Group", response.getCommunity_name());

        verify(communityRepository, times(1)).save(any(Community.class));
        verify(userRepository, times((1))).save(mockUser);
    }

    @Test
    void createCommunityUserNotFoundException() {
        when(userRepository.findUserByUsername(mockUser.getUsername())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> communityService.createCommunity(communityRequest, mockUser.getUsername()));
        verify(communityRepository, never()).save(any());
    }

    @Test
    void createCommunityCommunityAlreadyExistsException() {
        when(communityRepository.findByCommunityName("Tech Group")).thenReturn(Optional.of(mockCommunity));
        when(userRepository.findUserByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
        assertThrows(CommunityAlreadyExistsException.class, () -> communityService.createCommunity(communityRequest, mockUser.getUsername()));
        verify(userRepository, times(1)).findUserByUsername(mockUser.getUsername());
        verify(communityRepository, never()).save(any());
    }

    @Test
    void getCommunity() {
        when(communityRepository.findById(1L)).thenReturn(Optional.of(mockCommunity));
        when(communityMapper.convertCommunityToResponse(mockCommunity)).thenReturn(communityResponse);
        CommunityResponse response = communityService.getCommunity(1L);
        assertNotNull(response);
        assertEquals(mockCommunity.getCommunityName(), response.getCommunity_name());
        verify(communityRepository).findById(1L);
        verify(communityMapper).convertCommunityToResponse(mockCommunity);
    }

    @Test
    void getCommunityCommunityNotFound() {
        when(communityRepository.findById(1L)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> communityService.getCommunity(1L));
        assertEquals("Community entity with id : " + 1L + " not found", exception.getMessage());
        verify(communityRepository).findById(1L);
        verifyNoInteractions(communityMapper);
    }

    @Test
    void getCommunityByName() {
        when(communityRepository.findByCommunityName(mockCommunity.getCommunityName()))
                .thenReturn(Optional.of(mockCommunity));

        when(communityMapper.convertCommunityToResponse(mockCommunity))
                .thenReturn(communityResponse);

        // Act
        CommunityResponse response = communityService.getCommunityByName(mockCommunity.getCommunityName());

        // Assert
        assertNotNull(response);
        assertEquals(communityResponse, response);

        // Verify interactions
        verify(communityRepository).findByCommunityName(mockCommunity.getCommunityName());
        verify(communityMapper).convertCommunityToResponse(mockCommunity);
    }

    @Test
    void getCommunityByNameCommunityNotFound() {
        when(communityRepository.findByCommunityName("NonExistingCommunity"))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            communityService.getCommunityByName("NonExistingCommunity");
        });

        assertEquals("Community entity with name : NonExistingCommunity not found", exception.getMessage());

        // Verify interactions
        verify(communityRepository).findByCommunityName("NonExistingCommunity");
        verifyNoInteractions(communityMapper);
    }

    @Test
    void getAllCommunities() {
        // Arrange
        List<Community> communityList = List.of(mockCommunity);
        when(communityRepository.findAll()).thenReturn(communityList);
        when(communityMapper.convertCommunityToResponse(mockCommunity)).thenReturn(communityResponse);

        // Act
        List<CommunityResponse> responseList = communityService.getAllCommunities();

        // Assert
        assertNotNull(responseList);
        assertEquals(1, responseList.size());
        assertEquals(communityResponse, responseList.get(0));

        // Verify interactions
        verify(communityRepository).findAll();
        verify(communityMapper).convertCommunityToResponse(mockCommunity);
    }

    @Test
    void testGetAllCommunitiesEmptyList() {
        // Arrange
        when(communityRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            communityService.getAllCommunities();
        });

        assertEquals("There is no community", exception.getMessage());

        // Verify interactions
        verify(communityRepository).findAll();
        verifyNoInteractions(communityMapper);
    }

    @Test
    void updateCommunity() {
        // Arrange
        when(communityRepository.findByCommunityName("Tech Group")).thenReturn(Optional.of(mockCommunity));
        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(communityRepository.findByCommunityName("Updated Tech Group")).thenReturn(Optional.empty());
        when(communityRepository.save(any(Community.class))).thenReturn(mockCommunity);

        UpdateCommunityRequest updateRequest = new UpdateCommunityRequest();
        updateRequest.setCommunity_name("Updated Tech Group");
        updateRequest.setDescription("Updated description");

        CommunityResponse updatedResponse = new CommunityResponse();
        updatedResponse.setCommunity_name("Updated Tech Group");
        updatedResponse.setDescription("Updated description");

        when(communityMapper.convertCommunityToResponse(mockCommunity)).thenReturn(updatedResponse);

        // Act
        CommunityResponse response = communityService.updateCommunity("Tech Group", updateRequest, "testUser");

        // Assert
        assertNotNull(response);
        assertEquals("Updated Tech Group", response.getCommunity_name());
        assertEquals("Updated description", response.getDescription());

        // Verify interactions
        verify(communityRepository).findByCommunityName("Tech Group");
        verify(userRepository).findUserByUsername("testUser");
        verify(communityRepository).save(mockCommunity);
        verify(communityMapper).convertCommunityToResponse(mockCommunity);
    }

    @Test
    void updateCommunityCommunityNotFoundException() {
        String communityName = "NonExistingCommunity";
        UpdateCommunityRequest updateCommunityRequest = new UpdateCommunityRequest();
        updateCommunityRequest.setCommunity_name("Updated Community");

        String username = "testUser";

        when(communityRepository.findByCommunityName(communityName)).thenReturn(Optional.empty());

        // Act & Assert
        CommunityNotFoundException exception = assertThrows(CommunityNotFoundException.class, () -> {
            communityService.updateCommunity(communityName, updateCommunityRequest, username);
        });

        assertEquals("Community not found with community name : " + communityName, exception.getMessage());
    }

    @Test
    void updateCommunityUserNotFoundException() {
        String communityName = "Tech Group";
        UpdateCommunityRequest updateCommunityRequest = new UpdateCommunityRequest();
        updateCommunityRequest.setCommunity_name("Updated Community");

        String username = "nonExistingUser";

        when(communityRepository.findByCommunityName(communityName)).thenReturn(Optional.of(mockCommunity));
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            communityService.updateCommunity(communityName, updateCommunityRequest, username);
        });

        assertEquals("User not found with username : " + username, exception.getMessage());
    }

    @Test
    void updateCommunityUnauthorizedException() {
        // Arrange
        String communityName = "Tech Group";
        UpdateCommunityRequest updateCommunityRequest = new UpdateCommunityRequest();
        updateCommunityRequest.setCommunity_name("Updated Community");

        String username = "nonModeratorUser";

        mockCommunity.setCreator(new User());
        mockCommunity.setModerators(new ArrayList<>());

        when(communityRepository.findByCommunityName(communityName)).thenReturn(Optional.of(mockCommunity));
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act & Assert
        UnauthorizedActionException exception = assertThrows(UnauthorizedActionException.class, () -> {
            communityService.updateCommunity(communityName, updateCommunityRequest, username);
        });

        assertEquals("Unauthorized action", exception.getMessage());
    }

    @Test
    void getAllMembers() {
        User mockUser1 = new User();
        mockUser1.setUsername("user1");
        mockUser1.setEmail("user1@example.com");

        User mockUser2 = new User();
        mockUser2.setUsername("user2");
        mockUser2.setEmail("user2@example.com");

        List<User> mockUsers = Arrays.asList(mockUser1, mockUser2);

        when(communityRepository.findById(mockCommunity.getId())).thenReturn(Optional.of(mockCommunity));
        when(communityRepository.getAllMembersByUsingCommunityId(mockCommunity.getId())).thenReturn(mockUsers);

        List<UserDto> result = communityService.getAllMembers(mockCommunity.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
    }

    @Test
    void getAllMembers_shouldThrowIllegalArgumentException_whenCommunityNotFound() {
        // Arrange: Mock CommunityRepository to return an empty Optional when searching for the community.
        when(communityRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that an IllegalArgumentException is thrown when trying to get all members
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            communityService.getAllMembers(1L);
        });

        // Assert: Verify the exception message
        assertEquals("Community not found with id : 1", thrown.getMessage());
    }

    @Test
    void getMembersCount_shouldReturnCount_whenCommunityFound() {
        // Arrange: Mock CommunityRepository to return a community
        when(communityRepository.findById(1L)).thenReturn(Optional.of(mockCommunity));
        when(communityRepository.countApprovedUsersByCommunityId(1L)).thenReturn(5); // 5 üye

        // Act: Call the method
        int result = communityService.getMembersCount(1L);

        // Assert: Verify that the result is 5
        assertEquals(5, result);
    }

    @Test
    void getMembersCount_shouldThrowIllegalArgumentException_whenCommunityNotFound() {
        // Arrange: Mock CommunityRepository to return an empty Optional when searching for the community
        when(communityRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that an IllegalArgumentException is thrown
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            communityService.getMembersCount(1L);
        });

        // Assert: Verify the exception message
        assertEquals("Community not found with id : 1", thrown.getMessage());
    }

    @Test
    void findCommunityById_shouldReturnCommunity_whenCommunityExists() {
        // Arrange: Mock CommunityRepository to return an Optional with a mock Community
        when(communityRepository.findById(1L)).thenReturn(Optional.of(mockCommunity));

        // Act: Call the method
        Optional<Community> result = communityService.findCommunityById(1L);

        // Assert: Verify that the result contains the mock community
        assertTrue(result.isPresent());
        assertEquals(mockCommunity, result.get());
    }

    @Test
    void findCommunityById_shouldReturnEmptyOptional_whenCommunityNotFound() {
        // Arrange: Mock CommunityRepository to return an empty Optional when searching for the community
        when(communityRepository.findById(1L)).thenReturn(Optional.empty());

        // Act: Call the method
        Optional<Community> result = communityService.findCommunityById(1L);

        // Assert: Verify that the result is empty
        assertFalse(result.isPresent());
    }

    @Test
    void addUserToCommunity_shouldAddUser_whenUserNotAlreadyInCommunity() {
        // Arrange: Mock the necessary methods
        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(communityRepository.countUserInCommunity(mockCommunity.getId(), mockUser.getId())).thenReturn(0);
        when(communityRepository.save(mockCommunity)).thenReturn(mockCommunity);

        // Set missing fields to avoid null pointer exception
        mockCommunity.setAccess_level(AccessLevel.PUBLIC);  // Access level is set here to avoid NullPointerException
        mockCommunity.setCreatedAt(LocalDateTime.now());
        mockCommunity.setDescription("A description of the community");
        mockCommunity.setTopics(Arrays.asList("Java", "Spring", "Hibernate"));

        // Mocking the mapper
        CommunityResponseWithApprovedUsers mockResponse = new CommunityResponseWithApprovedUsers(
                mockCommunity.getId(),
                mockCommunity.getCommunityName(),
                mockCommunity.getDescription(),
                mockCommunity.getCreatedAt(),
                mockCommunity.getTopics(),
                mockCommunity.getAccess_level().toString(),
                Arrays.asList(
                        new UserDto("testUser", "testUser@example.com", LocalDateTime.now(), List.of(Role.USER))
                )
        );

        // Mock the mapping behavior of the mapper
        when(communityMapper.convertCommunityToResponseWithApprovedUsers(mockCommunity)).thenReturn(mockResponse);

        // Act: Call the method being tested
        CommunityResponseWithApprovedUsers actualResponse = communityService.addUserToCommunity(mockCommunity, "testUser");

        // Assert: Verify the result is not null and matches the expected response
        assertNotNull(actualResponse);
        assertEquals(mockResponse.getCreatorId(), actualResponse.getCreatorId());
        assertEquals(mockResponse.getCommunity_name(), actualResponse.getCommunity_name());
        assertEquals(mockResponse.getDescription(), actualResponse.getDescription());
        assertEquals(mockResponse.getCreatedAt(), actualResponse.getCreatedAt());
        assertEquals(mockResponse.getTopics(), actualResponse.getTopics());
        assertEquals(mockResponse.getAccess_level(), actualResponse.getAccess_level());
        assertEquals(mockResponse.getApprovedUsers().size(), actualResponse.getApprovedUsers().size());
        assertEquals(mockResponse.getApprovedUsers().get(0).getUsername(), actualResponse.getApprovedUsers().get(0).getUsername());
    }

    @Test
    void addUserToCommunity_shouldThrowIllegalArgumentException_whenUserAlreadyInCommunity() {
        // Arrange: Mock the necessary methods
        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(communityRepository.countUserInCommunity(mockCommunity.getId(), mockUser.getId())).thenReturn(1);

        // Act & Assert: Verify that an IllegalArgumentException is thrown when trying to add the user
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            communityService.addUserToCommunity(mockCommunity, "testUser");
        });

        // Assert: Verify the exception message
        assertEquals("User already is member of that community", thrown.getMessage());
    }



}