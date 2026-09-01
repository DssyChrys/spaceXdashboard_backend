package com.example.spaceXdashboard_backend.dto;

import lombok.Data;

@Data
public class KpisDto {

    private Long totalLancement;
    private Long tauxReussite;
    private String prochainLancement;
    private String dateProchainLancement;
}
