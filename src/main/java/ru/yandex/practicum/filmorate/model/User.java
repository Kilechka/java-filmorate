package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true, value = {"friends"})
public class User {

    private Long id;
    private String login;
    private String email;
    private LocalDate birthday;
    private String name;
    @JsonIgnoreProperties({"friends"})
    private Set<Long> friends = new HashSet<>();
}


