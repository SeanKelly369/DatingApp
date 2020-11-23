package org.meeboo.service;

import org.meeboo.domain.UserPrincipal;
import org.meeboo.entity.UserEntity;
import org.meeboo.enumeration.Role;
import org.meeboo.exception.*;
import org.meeboo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.meeboo.constant.FileConstant.*;
import static org.meeboo.constant.UserConstants.*;
import static org.meeboo.enumeration.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.MediaType.*;

@Service
@Transactional
@Qualifier("userDetailsService")
@Slf4j
public class UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService = new LoginAttemptService();

    @Autowired
    private EmailService emailService;

    @Autowired
    UserRepository userRepository;

    public static String confirmationToken = "";
    public static long registerDate;

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserDetails getUser(Long userId) throws UserIdNotFoundException {
        var user = userRepository.findByUserId(userId);
        if (user == null) {
            log.error(NO_USER_FOUND_BY_USER_ID);
            throw new UserIdNotFoundException(NO_USER_FOUND_BY_USER_ID);
        } else {
            validationLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            org.meeboo.domain.UserPrincipal userPrincipal = new UserPrincipal(user);
            return userPrincipal;
        }
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }


    private Long generateUserId() {
        return Long.valueOf(RandomStringUtils.randomNumeric(10));
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    // Register User
    public UserEntity register(String firstName, String lastName, String username, String email)
            throws UsernameNotFoundException, UsernameExistException, EmailExistException, MessagingException, UserNotFoundException, javax.mail.MessagingException {
        validateNewUsernameAndEmail(EMPTY, username, email);
        var userEntity = new UserEntity();
        var password = generatePassword();
        userEntity.setUserId(generateUserId());
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);
        userEntity.setUsername(username);
        userEntity.setEmail(email);
        userEntity.setJoinDate(new Date());
        userEntity.setPassword(encodePassword(password));
        userEntity.setActive(true);
        userEntity.setNotLocked(true);
        userEntity.setRole(ROLE_USER.name());
        userEntity.setAuthorities(ROLE_USER.getAuthorities());
        userEntity.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userEntity.setConfirmed(false);

        confirmationToken = generateToken();

        Calendar date = Calendar.getInstance();
        registerDate = date.getTimeInMillis();

        userRepository.save(userEntity);
        log.info("New user password: " + password);
        emailService.sendNewPasswordEMail(firstName, password, email, confirmationToken);
        return userEntity;
    }

    private String getTemporaryProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
    }

    private String encodePassword(String password) {
        var encodedPassword = passwordEncoder.encode(password);
        return encodedPassword;
    }

    private void validationLoginAttempt(UserEntity user) {
        if (user.isNotLocked()) {
            if (loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    public UserEntity findUserByUsername(String username) {
        return userRepository.findUserEntityByUsername(username);
    }

    private UserEntity findUserByEmail(String newEmail) {
        return userRepository.findUserByEmail(newEmail);
    }

    private UserEntity validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
        var userByNewUsername = findUserByUsername(newUsername);
        var userByNewEmail = findUserByEmail(newEmail);
        if (StringUtils.isNotBlank(currentUsername)) {
            var currentUser = findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
            }
            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if (userByNewUsername != null) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    // Add User
    public UserEntity addNewUser(String firstName, String lastName, String username, String email, String country, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        validateNewUsernameAndEmail(EMPTY, username, email);
        var userEntity = new UserEntity();
        var password = generatePassword();
        userEntity.setUserId(generateUserId());
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);
        userEntity.setJoinDate(new Date());
        userEntity.setUsername(username);
        userEntity.setEmail(email);
        userEntity.setCountry(country);
        userEntity.setPassword(encodePassword(password));
        userEntity.setActive(isActive);
        userEntity.setNotLocked(isNonLocked);
        userEntity.setRole(getRoleEnumName(role).name());
        userEntity.setAuthorities(getRoleEnumName(role).getAuthorities());
        userEntity.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(userEntity);
        saveProfileImage(userEntity, profileImage);
        log.info("New user password: {}", password);
        return userEntity;
    }


    private void saveProfileImage(UserEntity userEntity, MultipartFile profileImage) throws NotAnImageFileException, IOException {
        if (profileImage != null) {
            if(!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
                throw new NotAnImageFileException(profileImage.getOriginalFilename() + NOT_AN_IMAGE_FILE);
            }
            Path userFolder = Paths.get(USER_FOLDER + userEntity.getUsername()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                log.info(DIRECTORY_CREATED + userFolder);
            }
            Files.deleteIfExists(Paths.get(userFolder + userEntity.getUsername() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(userEntity.getUsername() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            userEntity.setProfileImageUrl(setProfileImageUrl(userEntity.getUsername()));
            userRepository.save(userEntity);
            log.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH + username + DOT + JPG_EXTENSION).toUriString();
    }

    public Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }


    // Update user
    public UserEntity updateUser(String currentUsername, String newFirstname, String newLastname, String newUsername, String newEmail, String country, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws
            UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        UserEntity currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
        currentUser.setFirstName(newFirstname);
        currentUser.setLastName(newLastname);
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setCountry(country);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setNotLocked(isNonLocked);
        currentUser.setActive(isActive);
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        userRepository.save(currentUser);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }
}
