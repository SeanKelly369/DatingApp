package org.meeboo.entity;

import lombok.Data;
import org.meeboo.model.UserPhoto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "Photos")
public class PhotoCountEntity {
    @Id
    private String id;
    private List<UserPhoto> photos;


}
