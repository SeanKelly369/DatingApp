package entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "User")
@Data
public class User {

    @Id
    private String userId;
    private String name;
    private String password;
    private Date creationDate = new Date();
    private Map<String, String> userSettings = new HashMap<>();

}
