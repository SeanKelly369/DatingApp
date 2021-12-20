package org.meeboo.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;

import java.util.Date;

@Getter
@Setter
public class UserPhoto {
    private int photoId;
    private Binary photo;
    private Date imageUploadDate = new Date();
}
