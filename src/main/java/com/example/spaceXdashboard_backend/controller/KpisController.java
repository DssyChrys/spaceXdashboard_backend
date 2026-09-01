package com.example.spaceXdashboard_backend.controller;

import com.example.spaceXdashboard_backend.dto.*;
import com.example.spaceXdashboard_backend.service.SpaceXService;
import com.example.spaceXdashboard_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class KpisController {

    private final SpaceXService spaceXService;

    @Autowired
    private UserService userService;

    public KpisController(SpaceXService spaceXService) {
        this.spaceXService = spaceXService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<?> getDashboardKpis() {
        try {
            return ResponseEntity.ok(spaceXService.genererDashboardKpis());
        } catch (Exception e) {
            log.error("Échec GET /kpis : {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Service externe indisponible, réessayez plus tard."));
        }
    }

    @GetMapping("/graph-data")
    public ResponseEntity<?> getHistoriqueLancements() {
        try {
            List<StatistiquesAnnuellesDto> stats =
                    spaceXService.obtenirStatistiquesParAnnees(List.of(2023, 2024, 2025, 2026));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Échec GET /graph-data : {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Service externe indisponible, réessayez plus tard."));
        }
    }

    @GetMapping("/lancements")
    public ResponseEntity<?> getLancements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Boolean succes) {
        try {
            return ResponseEntity.ok(
                    spaceXService.obtenirLancementsFiltres(page, taille, annee, succes)
            );
        } catch (Exception e) {
            log.error("Échec GET /lancements : {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Service externe indisponible, réessayez plus tard."));
        }
    }

    @GetMapping("/lancements/{id}")
    public ResponseEntity<?> getLancementDetails(@PathVariable String id) {
        try {
            LancementDetailDto detail = spaceXService.obtenirDetailsLancement(id);
            if (detail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Lancement introuvable."));
            }
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            log.error("Échec GET /lancements/{} : {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Service externe indisponible, réessayez plus tard."));
        }
    }

    @PostMapping("/admin/resync")
    public ResponseEntity<ResyncAdminDto> declencherResynchronisation() {
        // synchroniserDonneesManuellement() gère déjà ses propres erreurs en
        // interne (statut "ERREUR" dans le DTO), donc pas besoin de try/catch ici.
        return ResponseEntity.ok(spaceXService.synchroniserDonneesManuellement());
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsername(@RequestParam String email){
        String username = userService.getUsernameByEmail(email);
        return ResponseEntity.ok(username);
    }
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@RequestParam String email){
        ProfileResponse profile = userService.getProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

}