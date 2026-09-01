package com.example.spaceXdashboard_backend.dto;

import lombok.Data;

@Data
public class StatistiquesAnnuellesDto {

    private int annee;
    private Long totalLancements;
    private Long tauxSucces;

}
