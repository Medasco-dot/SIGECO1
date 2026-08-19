package com.carfo.contentieux.service;

import com.carfo.contentieux.dto.StatistiquesSynthese;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.model.TypeContentieux;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.repository.EtapeDossierRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calcule les indicateurs consolidés du tableau de bord (extrait de StatistiquesController,
 * qui ne conservait auparavant que le routage HTTP : l'agrégation des dossiers est de la
 * logique métier, pas du contrôle d'accès HTTP).
 */
@Service
public class StatistiquesService {

    private final DossierRepository dossierRepository;
    private final EtapeDossierRepository etapeDossierRepository;

    public StatistiquesService(DossierRepository dossierRepository, EtapeDossierRepository etapeDossierRepository) {
        this.dossierRepository = dossierRepository;
        this.etapeDossierRepository = etapeDossierRepository;
    }

    public StatistiquesSynthese synthese() {
        List<Dossier> tous = dossierRepository.findAll();
        StatistiquesSynthese s = new StatistiquesSynthese();

        s.setTotalDossiers(tous.size());

        Set<String> ouverts = dossierRepository.findDistinctByEtapes_DateFinIsNullAndEtapes_Etape(EtapeDossier.Etape.ouvert)
                .stream().map(Dossier::getNumeroDossier).collect(Collectors.toSet());
        Set<String> appel = dossierRepository.findDistinctByEtapes_DateFinIsNullAndEtapes_Etape(EtapeDossier.Etape.en_appel)
                .stream().map(Dossier::getNumeroDossier).collect(Collectors.toSet());
        Set<String> cassation = dossierRepository.findDistinctByEtapes_DateFinIsNullAndEtapes_Etape(EtapeDossier.Etape.en_cassation)
                .stream().map(Dossier::getNumeroDossier).collect(Collectors.toSet());

        s.setDossiersOuverts(ouverts.size());
        s.setDossiersEnAppel(appel.size());
        s.setDossiersEnCassation(cassation.size());
        s.setDossiersAlerte(0);

        BigDecimal risque = BigDecimal.ZERO;
        BigDecimal frais = BigDecimal.ZERO;
        BigDecimal reclame = BigDecimal.ZERO;
        for (Dossier d : tous) {
            if (d.getRisqueFinancier() != null) risque = risque.add(d.getRisqueFinancier());
            if (d.getFraisJustice() != null) frais = frais.add(d.getFraisJustice());
            if (d.getMontantReclame() != null) reclame = reclame.add(d.getMontantReclame());
        }
        s.setRisqueFinancierCumule(risque);
        s.setFraisJusticeCumules(frais);
        s.setMontantReclameCumule(reclame);

        Map<TypeContentieux.Nature, Long> typeMap = new EnumMap<>(TypeContentieux.Nature.class);
        for (Dossier d : tous) {
            if (d.getTypeContentieux() != null && d.getTypeContentieux().getNature() != null) {
                typeMap.merge(d.getTypeContentieux().getNature(), 1L, Long::sum);
            }
        }
        s.setRepartitionParType(typeMap);

        Map<String, EtapeDossier> latestByDossier = getLatestEtapeParDossier();
        Map<EtapeDossier.Etape, Long> etapeMap = new EnumMap<>(EtapeDossier.Etape.class);
        for (EtapeDossier ed : latestByDossier.values()) {
            etapeMap.merge(ed.getEtape(), 1L, Long::sum);
        }
        s.setRepartitionParEtape(etapeMap);

        List<StatistiquesSynthese.DossierLeger> top5 = tous.stream()
                .filter(d -> d.getRisqueFinancier() != null)
                .sorted(Comparator.comparing(Dossier::getRisqueFinancier).reversed())
                .limit(5)
                .map(d -> {
                    String etape = latestByDossier.get(d.getNumeroDossier()) != null
                            ? latestByDossier.get(d.getNumeroDossier()).getEtape().name() : "";
                    String type = d.getTypeContentieux() != null && d.getTypeContentieux().getNature() != null
                            ? d.getTypeContentieux().getNature().name() : "";
                    return new StatistiquesSynthese.DossierLeger(
                            d.getNumeroDossier(),
                            type,
                            d.getRisqueFinancier(),
                            etape);
                })
                .collect(Collectors.toList());
        s.setTop5Risques(top5);

        return s;
    }

    public long nombreTotalDossiers() {
        return dossierRepository.findAll().size();
    }

    public Map<TypeContentieux.Nature, Long> dossiersParType() {
        List<Dossier> tous = dossierRepository.findAll();
        Map<TypeContentieux.Nature, Long> typeMap = new EnumMap<>(TypeContentieux.Nature.class);
        for (Dossier d : tous) {
            if (d.getTypeContentieux() != null && d.getTypeContentieux().getNature() != null) {
                typeMap.merge(d.getTypeContentieux().getNature(), 1L, Long::sum);
            }
        }
        return typeMap;
    }

    public Map<EtapeDossier.Etape, Long> etapesParStatut() {
        Map<String, EtapeDossier> latestByDossier = getLatestEtapeParDossier();
        Map<EtapeDossier.Etape, Long> etapeMap = new EnumMap<>(EtapeDossier.Etape.class);
        for (EtapeDossier ed : latestByDossier.values()) {
            etapeMap.merge(ed.getEtape(), 1L, Long::sum);
        }
        return etapeMap;
    }

    private Map<String, EtapeDossier> getLatestEtapeParDossier() {
        Map<String, EtapeDossier> latestByDossier = new HashMap<>();
        for (EtapeDossier ed : etapeDossierRepository.findAll()) {
            String nd = ed.getDossier() != null ? ed.getDossier().getNumeroDossier() : null;
            if (nd == null) continue;
            EtapeDossier prev = latestByDossier.get(nd);
            if (prev == null || (ed.getDateDebut() != null
                    && (prev.getDateDebut() == null || ed.getDateDebut().isAfter(prev.getDateDebut())))) {
                latestByDossier.put(nd, ed);
            }
        }
        return latestByDossier;
    }
}
