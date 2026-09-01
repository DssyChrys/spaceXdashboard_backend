package com.example.spaceXdashboard_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SpaceXNextLaunchResponse {
    private String name;

    @JsonProperty("date_utc")
    private String dateUtc;
}