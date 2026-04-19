package com.david.xpup.backend.dto.rawg;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RawgSearchResponseDto {

    private List<RawgGameDto> results;
}