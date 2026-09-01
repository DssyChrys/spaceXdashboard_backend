package com.example.spaceXdashboard_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class FiltreLancementResponseDto {
    private Long totalElements;
    private int pageActuelle;
    private List<LancementResumeDto> lancements;
}
