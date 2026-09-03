package com.example.spaceXdashboard_backend.service;

import com.example.spaceXdashboard_backend.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class SpaceXService{
    private final RestClient spaceXClient;
    private final RestClient spaceDevsClient;

    public SpaceXService(
            @Qualifier("spaceXRestClient") RestClient spaceXClient,
            @Qualifier("spaceDevsRestClient") RestClient spaceDevsClient) {
        this.spaceXClient = spaceXClient;
        this.spaceDevsClient = spaceDevsClient;
    }

    @Cacheable("kpis")
    public KpisDto genererDashboardKpis() {
        KpisDto kpis = new KpisDto();
        CompletableFuture<SpaceDevsCountResponse> futureTotal = CompletableFuture.supplyAsync(() ->
                spaceDevsClient.get()
                        .uri("/launches/?limit=1")
                        .retrieve()
                        .body(SpaceDevsCountResponse.class)
        );
        CompletableFuture<SpaceDevsCountResponse> futureSucces = CompletableFuture.supplyAsync(() ->
                spaceDevsClient.get()
                        .uri("/launches/?status=3&limit=1")
                        .retrieve()
                        .body(SpaceDevsCountResponse.class)
        );
        CompletableFuture<SpaceXNextLaunchResponse> futureProchain = CompletableFuture.supplyAsync(() ->
                spaceXClient.get()
                        .uri("/launches/next")
                        .retrieve()
                        .body(SpaceXNextLaunchResponse.class)
        );

        try {
            CompletableFuture.allOf(futureTotal, futureSucces, futureProchain).join();
            SpaceDevsCountResponse totalData = futureTotal.join();
            SpaceDevsCountResponse succesData = futureSucces.join();
            SpaceXNextLaunchResponse prochainData = futureProchain.join();

            if (totalData != null && succesData != null && totalData.getCount() > 0) {
                long total = totalData.getCount();
                long succes = succesData.getCount();
                long taux = (succes * 100) / total;
                kpis.setTotalLancement(total);
                kpis.setTauxReussite(taux);
            } else {
                kpis.setTotalLancement(0L);
                kpis.setTauxReussite(0L);
            }

            if (prochainData != null) {
                kpis.setProchainLancement(prochainData.getName());
                kpis.setDateProchainLancement(prochainData.getDateUtc());
            } else {
                kpis.setProchainLancement("Donnée indisponible");
                kpis.setDateProchainLancement("Indisponible");
            }
        } catch (Exception e) {
            // Ne PAS retourner un DTO "d'erreur" ici : cette méthode est @Cacheable,
            // tout ce qu'elle "return" est mis en cache. On log puis on relance,
            // pour que Spring ne cache rien et que le prochain appel retente
            // réellement l'appel externe. Le contrôleur se charge de transformer
            // cette exception en réponse HTTP propre (503).
            log.error("Échec récupération KPIs : {}", e.getMessage(), e);
            throw new RuntimeException("Échec de récupération des KPIs depuis l'API externe", e);
        }

        return kpis;
    }

    @Cacheable("statistiques")
    public  List<StatistiquesAnnuellesDto> obtenirStatistiquesParAnnees(List<Integer> annees) {
        List<CompletableFuture<StatistiquesAnnuellesDto>> futures = new ArrayList<>();

        for (int annee : annees) {
            String debutAnnee = annee + "-01-01T00:00:00Z";
            String finAnnee = annee + "-12-31T23:59:59Z";

            CompletableFuture<StatistiquesAnnuellesDto> futureAnnee = CompletableFuture.supplyAsync(() -> {
                StatistiquesAnnuellesDto dto = new StatistiquesAnnuellesDto();
                dto.setAnnee(annee);

                try {

                    SpaceDevsCountResponse total = spaceDevsClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/launches/")
                                    .queryParam("net__gte", debutAnnee)
                                    .queryParam("net__lte", finAnnee)
                                    .queryParam("lsp__id", 121) // 121 = ID unique de SpaceX sur SpaceDevs
                                    .queryParam("limit", 1)
                                    .build())
                            .retrieve()
                            .body(SpaceDevsCountResponse.class);


                    SpaceDevsCountResponse succes = spaceDevsClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/launches/")
                                    .queryParam("net__gte", debutAnnee)
                                    .queryParam("net__lte", finAnnee)
                                    .queryParam("lsp__id", 121)
                                    .queryParam("status", 3) // 3 = Success
                                    .queryParam("limit", 1)
                                    .build())
                            .retrieve()
                            .body(SpaceDevsCountResponse.class);

                    if (total != null && succes != null && total.getCount() > 0) {
                        long totalCount = total.getCount();
                        long succesCount = succes.getCount();

                        dto.setTotalLancements(totalCount);
                        dto.setTauxSucces((succesCount * 100) / totalCount);
                    } else {
                        dto.setTotalLancements(0L);
                        dto.setTauxSucces(0L);
                    }
                } catch (Exception e) {
                    log.error("Échec récupération stats année {} : {}", annee, e.getMessage(), e);
                    dto.setTotalLancements(0L);
                    dto.setTauxSucces(0L);
                }
                return dto;
            });

            futures.add(futureAnnee);
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        List<StatistiquesAnnuellesDto> resultats = new ArrayList<>();
        for (CompletableFuture<StatistiquesAnnuellesDto> future : futures) {
            resultats.add(future.join());
        }

        return resultats;
    }

    @Cacheable(value = "lancements", key = "#page + '-' + #taille + '-' + #annee + '-' + #succes")
    public FiltreLancementResponseDto obtenirLancementsFiltres(int page, int taille, Integer annee, Boolean succes) {
        FiltreLancementResponseDto reponse = new FiltreLancementResponseDto();
        int offset = page * taille;

        SpaceDevsLaunchesApiResponse apiData = spaceDevsClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/launches/")
                            .queryParam("lsp__id", 121)
                            .queryParam("limit", taille)
                            .queryParam("offset", offset)
                            .queryParam("mode", "detailed");

                    if (annee != null) {
                        uriBuilder.queryParam("net__gte", annee + "-01-01T00:00:00Z")
                                .queryParam("net__lte", annee + "-12-31T23:59:59Z");
                    }
                    if (succes != null) {
                        uriBuilder.queryParam("status", succes ? 3 : "1,2,4,5,6,7,8");
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(SpaceDevsLaunchesApiResponse.class);

        if (apiData != null && apiData.getResults() != null) {
            reponse.setTotalElements(apiData.getCount());
            reponse.setPageActuelle(page);

            List<LancementResumeDto> listeConvertie = apiData.getResults().stream().map(launch -> {
                LancementResumeDto resume = new LancementResumeDto();
                resume.setId(launch.getId());
                resume.setNom(launch.getName());
                resume.setDateUtc(launch.getNet());
                resume.setRocket(launch.getRocket().getConfiguration().getFullName());

                String abbrev = launch.getStatus().getAbbrev();
                resume.setStatutSucces("Success".equalsIgnoreCase(abbrev) ? "Oui" : "Failure".equalsIgnoreCase(abbrev) ? "Non" : "En attente");

                if (launch.getVidUrls() != null && !launch.getVidUrls().isEmpty()) {
                    resume.setUrlVideo(launch.getVidUrls().get(0).getUrl());
                }
                if (launch.getInfoUrls() != null && !launch.getInfoUrls().isEmpty()) {
                    resume.setUrlArticle(launch.getInfoUrls().get(0).getUrl());
                }
                return resume;
            }).toList();

            reponse.setLancements(listeConvertie);
        }
        return reponse;
    }

    @Cacheable(value = "details_lancement", key = "#id")
    public LancementDetailDto obtenirDetailsLancement(String id) {

        SpaceDevsLaunchesApiResponse.LaunchDetail launch = spaceDevsClient.get()
                .uri("/launches/" + id +  "/?mode=detailed")
                .retrieve()
                .body(SpaceDevsLaunchesApiResponse.LaunchDetail.class);

        if (launch == null) return null;

        LancementDetailDto detail = new LancementDetailDto();
        detail.setId(launch.getId());
        detail.setNom(launch.getName());
        detail.setDateUtc(launch.getNet());

        if (launch.getRocket() != null && launch.getRocket().getConfiguration() != null) {
            detail.setRocket(launch.getRocket().getConfiguration().getFullName());
        }
        if (launch.getPad() != null) {
            detail.setPadNom(launch.getPad().getName());
            if (launch.getPad().getLocation() != null) {
                detail.setPadLocalisation(launch.getPad().getLocation().getName());
            }
        }

        if (launch.getMission() != null) {
            detail.setDescription(launch.getMission().getDescription());
            if (launch.getMission().getPayloads() != null) {
                detail.setChargesUtiles(launch.getMission().getPayloads().stream()
                        .map(SpaceDevsLaunchesApiResponse.Mission.Payload::getName).toList());
            }
        }

        if (launch.getVidUrls() != null && !launch.getVidUrls().isEmpty()) {
            detail.setUrlVideo(launch.getVidUrls().get(0).getUrl());
        }

        if (launch.getInfoUrls() != null) {
            for (SpaceDevsLaunchesApiResponse.InfoUrl info : launch.getInfoUrls()) {
                if (info.getUrl() != null && info.getUrl().contains("wikipedia")) {
                    detail.setUrlWikipedia(info.getUrl());
                } else if (info.getUrl() != null) {
                    detail.setUrlArticle(info.getUrl());
                }
            }
        }

        return detail;
    }

    @org.springframework.cache.annotation.CacheEvict(
            value = {"kpis", "statistiques", "details_lancement", "lancements"},
            allEntries = true
    )
    public ResyncAdminDto synchroniserDonneesManuellement() {
        ResyncAdminDto reponse = new ResyncAdminDto();
        try {
            KpisDto kpis = new KpisDto();

            SpaceDevsCountResponse totalData = spaceDevsClient.get()
                    .uri("/launches/?limit=1")
                    .retrieve()
                    .body(SpaceDevsCountResponse.class);

            SpaceDevsCountResponse succesData = spaceDevsClient.get()
                    .uri("/launches/?status=3&limit=1")
                    .retrieve()
                    .body(SpaceDevsCountResponse.class);

            SpaceXNextLaunchResponse prochainData = spaceXClient.get()
                    .uri("/launches/next")
                    .retrieve()
                    .body(SpaceXNextLaunchResponse.class);

            if (totalData != null && succesData != null && totalData.getCount() > 0) {
                long total = totalData.getCount();
                long succes = succesData.getCount();
                kpis.setTotalLancement(total);
                kpis.setTauxReussite((succes * 100) / total);
            } else {
                kpis.setTotalLancement(0L);
                kpis.setTauxReussite(0L);
            }

            if (prochainData != null) {
                kpis.setProchainLancement(prochainData.getName());
                kpis.setDateProchainLancement(prochainData.getDateUtc());
            }


            List<StatistiquesAnnuellesDto> statistiques = obtenirStatistiquesParAnnees(List.of(2023, 2024, 2025, 2026));

            reponse.setStatut("SUCCÈS");
            reponse.setMessage("Le cache a été purgé avec succès. Les données ont été rafraîchies depuis les APIs distantes.");
            reponse.setNouveauxKpis(kpis);
            reponse.setNouvelStatistiques(statistiques);

        } catch (Exception e) {
            log.error("Échec resynchronisation manuelle : {}", e.getMessage(), e);
            reponse.setStatut("ERREUR");
            reponse.setMessage("Échec de la resynchronisation réseau : " + e.getMessage());
        }
        return reponse;
    }

}