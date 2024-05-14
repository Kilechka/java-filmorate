package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    @JsonIgnoreProperties({"friends"})
    private Set<Long> friends = new HashSet<>();
    private Map<Long, FriendshipStatus> friendships = new HashMap<>();
}
enum FriendshipStatus {
    NOT_CONFIRMED,
    CONFIRMED
}

