package org.meeboo.controller;

import org.meeboo.constant.SecurityConstant;
import org.meeboo.domain.UserPrincipal;
import org.meeboo.entity.UserEntity;
import org.meeboo.exception.*;
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

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"https://localhost:4200", "http://localhost:4200", "https://meeboo.org", "https://www.meeboo.org"})
public class UserController {

    @Autowired
    UserService userService;

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
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity userEntity) throws javax.mail.MessagingException, UserNotFoundException, EmailExistException, UsernameExistException {
        UserEntity newUser = userService.register(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<UserEntity> addNewUser(@RequestParam("firstName") String firstName,
                                                 @RequestParam("lastName") String lastName,
                                                 @RequestParam("username") String username,
                                                 @RequestParam("email") String email,
                                                 @RequestParam("country") String country,
                                                 @RequestParam("role") String role,
                                                 @RequestParam("isActive") String isActive,
                                                 @RequestParam("isNonLocked") String isNonLocked,
                                                 @RequestParam(value = "profileImage", required = false)MultipartFile profileImage)
            throws IOException, UserNotFoundException, EmailExistException, UsernameExistException, NotAnImageFileException {
        UserEntity newUser = userService.addNewUser(firstName, lastName, username, email, country, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<UserEntity> update(@RequestParam("currentUsername") String currentUsername,
                                             @RequestParam("firstName") String firstName,
                                             @RequestParam("lastName") String lastName,
                                             @RequestParam("username") String username,
                                             @RequestParam("email") String email,
                                             @RequestParam("country") String country,
                                             @RequestParam("role") String role,
                                             @RequestParam("isActive") String isActive,
                                             @RequestParam("isNonLocked") String isNonLocked,
                                             @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws
            IOException, UserNotFoundException, EmailExistException, UsernameExistException, NotAnImageFileException {
        UserEntity updateUser = userService.updateUser(currentUsername, firstName, lastName, username, email, country, role, Boolean.parseBoolean(isNonLocked), Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstant.JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
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
        boolean isExpired = (UserService.registerDate + TEN_MINUTES_IN_MILLIS) < timeInMillis;
        return token.equals(UserService.confirmationToken) && !isExpired;
    }
}

