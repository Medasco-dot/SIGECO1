package com.carfo.contentieux.controller;

import com.carfo.contentieux.model.*;
import com.carfo.contentieux.model.ImplicationId;
import com.carfo.contentieux.model.DossierJuristeId;
import com.carfo.contentieux.model.DossierCabinetId;
import com.carfo.contentieux.repository.TypeContentieuxRepository;
import com.carfo.contentieux.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "TestData", description = "Endpoints utilitaires pour insérer les jeux de données de test (usage local uniquement)")
@RestController
@RequestMapping("/api/test")
@ConditionalOnProperty(name = "app.test-data.enabled", havingValue = "true")
public class TestDataController {

    private final DossierService dossierService;
    private final TypeContentieuxRepository typeContentieuxRepository;
    private final PartieService partieService;
    private final ImplicationService implicationService;
    private final DossierJuristeService dossierJuristeService;
    private final DossierCabinetService dossierCabinetService;
    private final CabinetService cabinetService;
    private final DocumentService documentService;
    private final AudienceDecisionService audienceDecisionService;
    private final EtapeDossierService etapeDossierService;
    private final JuristeService juristeService;

    public TestDataController(DossierService dossierService,
                              TypeContentieuxRepository typeContentieuxRepository,
                              PartieService partieService,
                              ImplicationService implicationService,
                              DossierJuristeService dossierJuristeService,
                              DossierCabinetService dossierCabinetService,
                              CabinetService cabinetService,
                              DocumentService documentService,
                              AudienceDecisionService audienceDecisionService,
                              EtapeDossierService etapeDossierService,
                              JuristeService juristeService) {
        this.dossierService = dossierService;
        this.typeContentieuxRepository = typeContentieuxRepository;
        this.partieService = partieService;
        this.implicationService = implicationService;
        this.dossierJuristeService = dossierJuristeService;
        this.dossierCabinetService = dossierCabinetService;
        this.cabinetService = cabinetService;
        this.documentService = documentService;
        this.audienceDecisionService = audienceDecisionService;
        this.etapeDossierService = etapeDossierService;
        this.juristeService = juristeService;
    }

    @Operation(summary = "Insérer le jeu de données TEST (idempotent)")
    @PostMapping("/setup")
    public ResponseEntity<Map<String, Object>> setupTestData() {
        Map<String, Object> result = new HashMap<>();

        // 1) Dossier TEST-2026-0001
        TypeContentieux tc1 = typeContentieuxRepository.findById(1).orElse(null);
        Dossier d1 = new Dossier();
        d1.setNumeroDossier("TEST-2026-0001");
        d1.setResumeAffaire("Dossier test — vérification double clic et séquences CRUD");
        d1.setObservation("Créé automatiquement dans le cadre des tests fonctionnels");
        d1.setDateOuverture(LocalDate.parse("2026-08-11"));
        d1.setRisqueFinancier(BigDecimal.valueOf(500000));
        d1.setMontantReclame(BigDecimal.valueOf(300000));
        if (tc1 != null) d1.setTypeContentieux(tc1);
        Dossier savedD1 = null;
        try { savedD1 = dossierService.createDossier(d1); } catch (Exception ex) { /* ignore if exists */ savedD1 = dossierService.getDossierById("TEST-2026-0001").orElse(null); }
        result.put("dossier1", savedD1!=null?savedD1.getNumeroDossier():null);

        // 1b) Second dossier without numero (auto-generated)
        TypeContentieux tc2 = typeContentieuxRepository.findById(2).orElse(null);
        Dossier d2 = new Dossier();
        d2.setResumeAffaire("Dossier test 2 — vérification génération automatique du numéro");
        d2.setDateOuverture(LocalDate.parse("2026-08-11"));
        if (tc2 != null) d2.setTypeContentieux(tc2);
        Dossier savedD2 = dossierService.createDossier(d2);
        result.put("dossier2", savedD2!=null?savedD2.getNumeroDossier():null);

        // 2) Partie
        Partie p = new Partie();
        p.setNom("Traore"); p.setPrenom("Test"); p.setNumeroCnib("T00000001"); p.setStatutMatrimonial(Partie.StatutMatrimonial.celibataire);
        Partie savedP = null;
        try { savedP = partieService.createPartie(p); } catch (Exception ex) { /* ignore */ }
        result.put("partieId", savedP!=null?savedP.getId():null);

        // 2b) Implication
        if (savedD1 != null && savedP != null) {
            Implication impl = new Implication();
            impl.setId(new ImplicationId(savedD1.getNumeroDossier(), savedP.getId()));
            impl.setDossier(savedD1);
            impl.setPartie(savedP);
            impl.setRole(Implication.Role.demandeur);
            impl.setLienParente(Implication.LienParente.assure);
            try { implicationService.createImplication(impl); } catch (Exception ex) { }
        }

        // 3) Dossier-Juriste association (reuse existing matricule CARFO-J002 if present)
        String jurMat = "CARFO-J002";
        if (savedD1 != null) {
            DossierJuriste dj = new DossierJuriste();
            dj.setId(new DossierJuristeId(savedD1.getNumeroDossier(), jurMat));
            dj.setDossier(savedD1);
            juristeService.getJuristeById(jurMat).ifPresentOrElse(j -> dj.setJuriste(j), () -> {});
            try { dossierJuristeService.createDossierJuriste(dj); } catch (Exception ex) { }
        }

        // 4) Cabinet + association
        Cabinet cab = new Cabinet();
        cab.setIdentifiantCabinet("TEST-CAB-01"); cab.setNomCabinet("Cabinet Test & Associés"); cab.setMail("contact@cabinet-test.bf"); cab.setTelephone("+226 70 00 00 01"); cab.setAdresse("Ouagadougou, secteur test");
        try { cabinetService.createCabinet(cab); } catch (Exception ex) { }
        if (savedD1 != null) {
            DossierCabinet dc = new DossierCabinet();
            dc.setId(new DossierCabinetId(savedD1.getNumeroDossier(), cab.getIdentifiantCabinet()));
            dc.setDossier(savedD1);
            dc.setCabinet(cab);
            dc.setNomAvocatReferent("Maître Test Referent");
            try { dossierCabinetService.createDossierCabinet(dc); } catch (Exception ex) { }
        }

        // 5) Documents: create one per type listed
        if (savedD1 != null) {
            String[] types = new String[] {"requete","piece_justificative","pv_audience","releve_general_service","indice","acte_carriere","assignation","convocation","decision_justice"};
            for (String t : types) {
                Document doc = new Document();
                doc.setTypeDocument(t);
                doc.setDateAjout(LocalDate.parse("2026-08-11"));
                doc.setFichier("/test/" + (t.equals("requete")?"requete.pdf": t + ".pdf"));
                doc.setDossier(savedD1);
                try { documentService.createDocument(doc); } catch (Exception ex) { }
            }
            // document without fichier
            Document docNoFile = new Document();
            docNoFile.setTypeDocument("requete"); docNoFile.setDateAjout(LocalDate.parse("2026-08-11")); docNoFile.setDossier(savedD1);
            try { documentService.createDocument(docNoFile); } catch (Exception ex) { }
        }

        // 6) Audience (create then update decision)
        if (savedD1 != null) {
            AudienceDecision aud = new AudienceDecision();
            aud.setDate(LocalDate.parse("2026-08-15")); aud.setLieuAudience("Tribunal du travail de Ouagadougou (test)"); aud.setTypeEtape(AudienceDecision.TypeEtape.premiere_instance); aud.setDossier(savedD1);
            AudienceDecision savedAud = null;
            try { savedAud = audienceDecisionService.createAudienceDecision(aud); } catch (Exception ex) { }
            if (savedAud != null) {
                AudienceDecision upd = new AudienceDecision();
                upd.setNatureDecision(AudienceDecision.NatureDecision.jugement);
                upd.setResumeDecision("Décision test — jugement rendu");
                upd.setIssuePourCarfo(AudienceDecision.IssuePourCarfo.favorable);
                upd.setMontantObtenu(BigDecimal.valueOf(150000));
                upd.setFraisJustice(BigDecimal.valueOf(20000));
                try { audienceDecisionService.updateAudienceDecision(savedAud.getNumAudienceDecision(), upd); } catch (Exception ex) { }
            }
        }

        // 7) Etape de dossier
        if (savedD1 != null) {
            EtapeDossier et = new EtapeDossier();
            et.setEtape(EtapeDossier.Etape.en_instruction);
            et.setDateDebut(LocalDate.parse("2026-08-16"));
            et.setDossier(savedD1);
            try { etapeDossierService.createEtapeDossier(et); } catch (Exception ex) { }
        }

        result.put("status","ok");
        return ResponseEntity.ok(result);
    }
}
