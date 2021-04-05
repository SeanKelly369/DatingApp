package org.meeboo.controller;

import org.meeboo.domain.UserPrincipal;
import org.meeboo.entity.UserEntity;
import org.meeboo.exception.*;
import org.meeboo.model.UpdateUserModel;
import org.meeboo.service.UserService;
import org.meeboo.utility.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static org.meeboo.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.meeboo.service.UserService.confirmationToken;
import static org.meeboo.service.UserService.registerDate;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    UpdateUserModel updateUserModel;

    private AuthenticationManager authenticationManager;

    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/users")
    public List<UserEntity> showUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/user/{userId}")
    public UserDetails showUser(@PathVariable("userId") Long userId) throws UserIdNotFoundException {
        return userService.getUser(userId);
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity userEntity) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException, javax.mail.MessagingException, UserIdNotFoundException {
        UserEntity newUser = userService.register(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PostMapping("/add")
    public ResponseEntity<UserEntity> addNewUser(@RequestParam("firstName") String firstName,
                                                 @RequestParam("lastName") String lastName,
                                                 @RequestParam("username") String username,
                                                 @RequestParam("email") String email,
                                                 @RequestParam("country") String country,
                                                 @RequestParam("role") String role,
                                                 @RequestParam("isActive") boolean isActive,
                                                 @RequestParam("isNonLocked") boolean isNonLocked,
                                                 @RequestParam(value = "profileImage", required = false)MultipartFile profileImage)
        throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {

        updateUserModel.setNewFirstname(firstName);
        updateUserModel.setNewLastname(lastName);
        updateUserModel.setNewUsername(username);
        updateUserModel.setNewEmail(email);
        updateUserModel.setCountry(country);
        updateUserModel.setRole(role);
        updateUserModel.setActive(isActive);
        updateUserModel.setNonLocked(isNonLocked);
        updateUserModel.setProfileImage(profileImage);

        UserEntity newUser = userService.addNewUser(updateUserModel);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<UserEntity> update(@RequestParam("currentUsername") String currentUsername,
                                             @RequestParam("firstName") String firstName,
                                             @RequestParam("lastName") String lastName,
                                             @RequestParam("username") String username,
                                             @RequestParam("email") String email,
                                             @RequestParam("country") String country,
                                             @RequestParam("role") String role,
                                             @RequestParam("isActive") boolean isActive,
                                             @RequestParam("isNonLocked") boolean isNonLocked,
                                             @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws
            UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        UpdateUserModel updateUserModel = new UpdateUserModel();
        updateUserModel.setCurrentUsername(currentUsername);
        updateUserModel.setNewFirstname(firstName);
        updateUserModel.setNewLastname(lastName);
        updateUserModel.setNewUsername(username);
        updateUserModel.setNewEmail(email);
        updateUserModel.setCountry(country);
        updateUserModel.setRole(role);
        updateUserModel.setActive(isActive);
        updateUserModel.setNonLocked(isNonLocked);
        updateUserModel.setProfileImage(profileImage);

        UserEntity updateUser = userService.updateUser(updateUserModel);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }

    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity userEntity) {
        authenticate(userEntity.getUsername(), userEntity.getPassword());
        var loginUser = userService.findUserByUsername(userEntity.getUsername());
        var userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    @CrossOrigin
    @GetMapping(value = "/confirm-account", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean confirm(@RequestParam("token") String token) {
        final long TEN_MINUTES_IN_MILLIS = 600000;
        Calendar date = Calendar.getInstance();
        long timeInMillis = date.getTimeInMillis();
        boolean isExpired = (registerDate + TEN_MINUTES_IN_MILLIS) < timeInMillis;
        return token.equals(confirmationToken) && !isExpired;
    }
}

