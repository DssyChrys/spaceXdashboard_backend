package com.example.spaceXdashboard_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResyncAdminDto {
    private String statut;
    private String message;
    private KpisDto nouveauxKpis;
    private List<StatistiquesAnnuellesDto> nouvelStatistiques;
}
