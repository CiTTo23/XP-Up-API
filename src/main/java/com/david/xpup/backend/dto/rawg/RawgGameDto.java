package com.david.xpup.backend.dto.rawg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawgGameDto {

    private Integer id;

    private String name;

    @JsonProperty("background_image")
    private String backgroundImage;

    private String released;
}