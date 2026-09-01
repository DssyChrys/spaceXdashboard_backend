package com.example.spaceXdashboard_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class LancementDetailDto {
    private String id;
    private String nom;
    private String description;
    private String dateUtc;
    private String rocket;
    private String padNom;
    private String padLocalisation;
    private String urlVideo;
    private String urlArticle;
    private String urlWikipedia;
    private List<String> chargesUtiles; // Liste des noms des payloads (satellites, etc.)
}
