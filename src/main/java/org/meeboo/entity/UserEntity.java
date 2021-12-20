package org.meeboo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "Users")
public class UserEntity {

    @Id
    private String id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String profileImageUrl;
    private Date lastLoginDate;
    private Date lastLoginDateDisplay;
    private Date joinDate;
    private String role;
    private List<String> roles;
    private List<String> authorities;
    private boolean isActive;
    private boolean isNotLocked;
    private String country;
    private boolean isConfirmed;
}
