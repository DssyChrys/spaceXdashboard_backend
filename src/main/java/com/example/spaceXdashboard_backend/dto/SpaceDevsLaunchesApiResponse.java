package com.example.spaceXdashboard_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class SpaceDevsLaunchesApiResponse {
    private Long count;
    private List<LaunchDetail> results;

    @Data
    public static class LaunchDetail {
        private String id;
        private String name;
        private String net;
        private Status status;
        private Rocket rocket;
        private Pad pad;
        private Mission mission;

        // CORRECTION : Mappage des underscores de l'API externe
        @JsonProperty("info_urls")
        private List<InfoUrl> infoUrls;

        @JsonProperty("vid_urls")
        private List<VidUrl> vidUrls;
    }

    @Data public static class Status { private String abbrev; }
    @Data public static class Rocket { private Configuration configuration; }
    @Data
    public static class Configuration {
        @JsonProperty("full_name") // Gère le format JSON full_name
        private String fullName;
    }
    @Data public static class InfoUrl { private String url; }
    @Data public static class VidUrl { private String url; }

    @Data
    public static class Pad {
        private String name;
        private Location location;
        @Data public static class Location { private String name; }
    }

    @Data
    public static class Mission {
        private String description;
        private List<Payload> payloads;
        @Data public static class Payload { private String name; }
    }
}
