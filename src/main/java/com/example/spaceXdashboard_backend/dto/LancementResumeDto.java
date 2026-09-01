package com.example.spaceXdashboard_backend.dto;

import lombok.Data;

@Data
public class LancementResumeDto {
    private String id;
    private String nom;
    private String dateUtc;
    private String statutSucces; // "Oui", "Non", ou "En attente"
    private String rocket;
    private String image;
    private String urlArticle;
    private String urlVideo;
}
