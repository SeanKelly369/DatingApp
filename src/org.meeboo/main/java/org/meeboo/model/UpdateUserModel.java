package org.meeboo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class UpdateUserModel {
    private String currentUsername;
    private String newFirstname;
    private String newLastname;
    private String newUsername;
    private String newEmail;
    private String country;
    private String role;
    private boolean isActive;
    private boolean isNonLocked;
    private MultipartFile profileImage;
    private List<UserPhoto> photos;
}
