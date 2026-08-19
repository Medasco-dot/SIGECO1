package com.carfo.contentieux.service;

import com.carfo.contentieux.dto.DossierSearchCriteria;
import com.carfo.contentieux.exception.DuplicateResourceException;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.carfo.contentieux.model.AudienceDecision;
import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.TypeContentieux;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.repository.AudienceDecisionRepository;
import com.carfo.contentieux.repository.DossierRepository;
import com.carfo.contentieux.repository.EtapeDossierRepository;
import com.carfo.contentieux.repository.DossierCabinetRepository;
import com.carfo.contentieux.repository.DossierJuristeRepository;
import com.carfo.contentieux.repository.DocumentRepository;
import com.carfo.contentieux.repository.ImplicationRepository;
import com.carfo.contentieux.model.AuditLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import io.micrometer.core.instrument.MeterRegistry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DossierService {

    private final DossierRepository dossierRepository;
    private final EtapeDossierRepository etapeDossierRepository;
    private final DossierCabinetRepository dossierCabinetRepository;
    private final DossierJuristeRepository dossierJuristeRepository;
    private final AudienceDecisionRepository audienceDecisionRepository;
    private final DocumentRepository documentRepository;
    private final ImplicationRepository implicationRepository;
    private final AuditService auditService;
    private final Object numeroDossierGenerationLock = new Object();

    @PersistenceContext
    private EntityManager entityManager;

    private final MeterRegistry meterRegistry;
    private final TransactionTemplate transactionTemplate;

    public DossierService(DossierRepository dossierRepository,
                          EtapeDossierRepository etapeDossierRepository,
                          DossierCabinetRepository dossierCabinetRepository,
                          DossierJuristeRepository dossierJuristeRepository,
                          AudienceDecisionRepository audienceDecisionRepository,
                          DocumentRepository documentRepository,
                          ImplicationRepository implicationRepository,
                          AuditService auditService,
                          MeterRegistry meterRegistry,
                          PlatformTransactionManager transactionManager) {
        this.dossierRepository = dossierRepository;
        this.etapeDossierRepository = etapeDossierRepository;
        this.dossierCabinetRepository = dossierCabinetRepository;
        this.dossierJuristeRepository = dossierJuristeRepository;
        this.audienceDecisionRepository = audienceDecisionRepository;
        this.documentRepository = documentRepository;
        this.implicationRepository = implicationRepository;
        this.auditService = auditService;
        this.meterRegistry = meterRegistry;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
    }

    public List<Dossier> getAllDossiers() {
        return dossierRepository.findAll();
    }

    public Optional<Dossier> getDossierById(String numeroDossier) {
        return dossierRepository.findById(numeroDossier);
    }

    public Dossier createDossier(Dossier dossier) {
        com.carfo.contentieux.util.DateGuard.checkReasonable(dossier.getDateOuverture(), "dateOuverture");
        final int MAX_ATTEMPTS = 5;
        int attempt = 0;
        Dossier saved = null;

        if (dossier.getNumeroDossier() != null && !dossier.getNumeroDossier().isBlank()) {
            if (dossierRepository.existsById(dossier.getNumeroDossier())) {
                throw new DuplicateResourceException("Un dossier avec le numéro " + dossier.getNumeroDossier() + " existe déjà.");
            }
            try {
                saved = dossierRepository.saveAndFlush(dossier);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateResourceException("Un dossier avec le numéro " + dossier.getNumeroDossier() + " existe déjà.");
            }

            List<EtapeDossier> existingEtapes = etapeDossierRepository.findByDossier_NumeroDossier(saved.getNumeroDossier());
            if (existingEtapes == null || existingEtapes.isEmpty()) {
                EtapeDossier initial = new EtapeDossier();
                initial.setEtape(EtapeDossier.Etape.ouvert);
                initial.setDateDebut(saved.getDateOuverture() != null ? saved.getDateOuverture() : java.time.LocalDate.now());
                initial.setDossier(saved);
                etapeDossierRepository.save(initial);
            }
            auditService.enregistrer(AuditLog.Action.CREATE, "Dossier", saved.getNumeroDossier(),
                    "numeroDossier fourni par le client");
            return saved;
        }

        synchronized (numeroDossierGenerationLock) {
            while (attempt < MAX_ATTEMPTS) {
                attempt++;
                dossier.setNumeroDossier(genererNumeroDossier(dossier.getDateOuverture()));
                try {
                    saved = transactionTemplate.execute(status -> {
                        Dossier persisted = dossierRepository.saveAndFlush(dossier);
                        List<EtapeDossier> existingEtapes = etapeDossierRepository.findByDossier_NumeroDossier(persisted.getNumeroDossier());
                        if (existingEtapes == null || existingEtapes.isEmpty()) {
                            EtapeDossier initial = new EtapeDossier();
                            initial.setEtape(EtapeDossier.Etape.ouvert);
                            initial.setDateDebut(persisted.getDateOuverture() != null ? persisted.getDateOuverture() : java.time.LocalDate.now());
                            initial.setDossier(persisted);
                            etapeDossierRepository.save(initial);
                        }
                        return persisted;
                    });
                    if (saved == null) {
                        throw new DuplicateResourceException("Impossible de générer un numéro unique après " + MAX_ATTEMPTS + " essais.");
                    }
                    if (attempt > 1) {
                        meterRegistry.counter("dossier.numero.generation.retries").increment(attempt - 1);
                    }
                    break;
                } catch (DataIntegrityViolationException ex) {
                    meterRegistry.counter("dossier.numero.generation.collisions").increment();
                    org.slf4j.LoggerFactory.getLogger(DossierService.class)
                            .warn("Generated numeroDossier caused DataIntegrityViolation (attempt {}): {}", attempt, dossier.getNumeroDossier());
                    dossier.setNumeroDossier(null);
                    if (attempt >= MAX_ATTEMPTS) {
                        throw new DuplicateResourceException("Impossible de générer un numéro unique après " + MAX_ATTEMPTS + " essais.");
                    }
                }
            }
        }

        if (saved != null) {
            auditService.enregistrer(AuditLog.Action.CREATE, "Dossier", saved.getNumeroDossier(), "numero genere automatiquement");
        }
        return saved;
    }

    private String genererNumeroDossier(LocalDate dateOuverture) {
        LocalDate reference = dateOuverture != null ? dateOuverture : LocalDate.now();
        int year = reference.getYear();
        int sequence = 1;
        String candidat;
        do {
            candidat = String.format("DOS-%d-%04d", year, sequence);
            sequence++;
        } while (dossierRepository.existsById(candidat));
        return candidat;
    }

    public Dossier updateDossier(String numeroDossier, Dossier dossier) {
        Dossier existant = dossierRepository.findById(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + numeroDossier));

        List<String> champsModifies = new ArrayList<>();
        if (dossier.getDateOuverture() != null) {
            com.carfo.contentieux.util.DateGuard.checkReasonable(dossier.getDateOuverture(), "dateOuverture");
            existant.setDateOuverture(dossier.getDateOuverture());
            champsModifies.add("dateOuverture");
        }
        if (dossier.getResumeAffaire() != null) {
            existant.setResumeAffaire(dossier.getResumeAffaire());
            champsModifies.add("resumeAffaire");
        }
        if (dossier.getObservation() != null) {
            existant.setObservation(dossier.getObservation());
            champsModifies.add("observation");
        }
        if (dossier.getRisqueFinancier() != null) {
            existant.setRisqueFinancier(dossier.getRisqueFinancier());
            champsModifies.add("risqueFinancier");
        }
        if (dossier.getMontantReclame() != null) {
            existant.setMontantReclame(dossier.getMontantReclame());
            champsModifies.add("montantReclame");
        }
        if (dossier.getFraisJustice() != null) {
            existant.setFraisJustice(dossier.getFraisJustice());
            champsModifies.add("fraisJustice");
        }
        if (dossier.getTypeContentieux() != null) {
            existant.setTypeContentieux(dossier.getTypeContentieux());
            champsModifies.add("typeContentieux");
        }
        Dossier sauvegarde = dossierRepository.save(existant);
        auditService.enregistrer(AuditLog.Action.UPDATE, "Dossier", numeroDossier,
                "champs modifies: " + String.join(", ", champsModifies));
        return sauvegarde;
    }

    @Transactional
    public void recalculerFraisJustice(String numeroDossier) {
        Dossier dossier = dossierRepository.findById(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec le numéro " + numeroDossier));
        List<AudienceDecision> decisions = audienceDecisionRepository.findByEtapeDossier_Dossier_NumeroDossier(numeroDossier);
        BigDecimal total = BigDecimal.ZERO;
        if (decisions != null) {
            for (AudienceDecision d : decisions) {
                if (d.getFraisJustice() != null) {
                    total = total.add(d.getFraisJustice());
                }
            }
        }
        dossier.setFraisJustice(total);
        dossierRepository.save(dossier);
    }

    public List<Dossier> rechercherParNumero(String numeroDossier) {
        return dossierRepository.findByNumeroDossierContainingIgnoreCase(numeroDossier);
    }

    public List<Dossier> rechercherParType(TypeContentieux.Nature nature) {
        return dossierRepository.findByTypeContentieux_Nature(nature);
    }

    public List<Dossier> rechercherParPartie(String nomPartie) {
        return dossierRepository.findByPartie(nomPartie);
    }

    public List<Dossier> rechercherParStatut(EtapeDossier.Etape etape) {
        return dossierRepository.findDistinctByEtapes_DateFinIsNullAndEtapes_Etape(etape);
    }

    public Page<Dossier> rechercherMulticritere(DossierSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Dossier> query = cb.createQuery(Dossier.class);
        Root<Dossier> dossier = query.from(Dossier.class);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getNumeroDossier() != null && !criteria.getNumeroDossier().isBlank()) {
            predicates.add(cb.like(cb.lower(dossier.get("numeroDossier")),
                    "%" + criteria.getNumeroDossier().toLowerCase() + "%"));
        }

        if (criteria.getTypeContentieux() != null) {
            Join<Object, Object> typeJoin = dossier.join("typeContentieux");
            predicates.add(cb.equal(typeJoin.get("nature"), criteria.getTypeContentieux()));
        }

        if (criteria.getEtapeCourante() != null) {
            Join<Object, Object> etapesJoin = dossier.join("etapes");
            predicates.add(cb.equal(etapesJoin.get("etape"), criteria.getEtapeCourante()));
            predicates.add(cb.isNull(etapesJoin.get("dateFin")));
        }

        if (criteria.getNomPartie() != null && !criteria.getNomPartie().isBlank()) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<?> implicationRoot = subquery.from(entityManager.getMetamodel().entity("Implication").getJavaType());
            Join<?, ?> partieJoin = implicationRoot.join("partie");
            subquery.select(cb.literal(1L))
                    .where(cb.and(
                            cb.equal(implicationRoot.get("dossier"), dossier),
                            cb.or(
                                    cb.like(cb.lower(partieJoin.get("nom")), "%" + criteria.getNomPartie().toLowerCase() + "%"),
                                    cb.like(cb.lower(partieJoin.get("prenom")), "%" + criteria.getNomPartie().toLowerCase() + "%")
                            )
                    ));
            predicates.add(cb.exists(subquery));
        }

        if (criteria.getDateOuvertureMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(dossier.get("dateOuverture"), criteria.getDateOuvertureMin()));
        }

        if (criteria.getDateOuvertureMax() != null) {
            predicates.add(cb.lessThanOrEqualTo(dossier.get("dateOuverture"), criteria.getDateOuvertureMax()));
        }

        if (criteria.getRisqueFinancierMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(dossier.get("risqueFinancier"), criteria.getRisqueFinancierMin()));
        }

        if (criteria.getRisqueFinancierMax() != null) {
            predicates.add(cb.lessThanOrEqualTo(dossier.get("risqueFinancier"), criteria.getRisqueFinancierMax()));
        }

        if (criteria.getMatriculeJuriste() != null && !criteria.getMatriculeJuriste().isBlank()) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<?> djRoot = subquery.from(entityManager.getMetamodel().entity("DossierJuriste").getJavaType());
            Join<?, ?> juristeJoin = djRoot.join("juriste");
            subquery.select(cb.literal(1L))
                    .where(cb.and(
                            cb.equal(djRoot.get("dossier"), dossier),
                            cb.equal(juristeJoin.get("matricule"), criteria.getMatriculeJuriste())
                    ));
            predicates.add(cb.exists(subquery));
        }

        if (criteria.getIdentifiantCabinet() != null && !criteria.getIdentifiantCabinet().isBlank()) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<?> dcRoot = subquery.from(entityManager.getMetamodel().entity("DossierCabinet").getJavaType());
            Join<?, ?> cabinetJoin = dcRoot.join("cabinet");
            subquery.select(cb.literal(1L))
                    .where(cb.and(
                            cb.equal(dcRoot.get("dossier"), dossier),
                            cb.equal(cabinetJoin.get("identifiantCabinet"), criteria.getIdentifiantCabinet())
                    ));
            predicates.add(cb.exists(subquery));
        }

        query.where(predicates.toArray(new Predicate[0])).distinct(true);
        query.orderBy(cb.asc(dossier.get("numeroDossier")));

        TypedQuery<Dossier> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Dossier> results = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Dossier> countRoot = countQuery.from(Dossier.class);
        List<Predicate> countPredicates = new ArrayList<>();

        if (criteria.getNumeroDossier() != null && !criteria.getNumeroDossier().isBlank()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("numeroDossier")),
                    "%" + criteria.getNumeroDossier().toLowerCase() + "%"));
        }

        if (criteria.getTypeContentieux() != null) {
            Join<Object, Object> typeJoin = countRoot.join("typeContentieux");
            countPredicates.add(cb.equal(typeJoin.get("nature"), criteria.getTypeContentieux()));
        }

        if (criteria.getEtapeCourante() != null) {
            Join<Object, Object> etapesJoin = countRoot.join("etapes");
            countPredicates.add(cb.equal(etapesJoin.get("etape"), criteria.getEtapeCourante()));
            countPredicates.add(cb.isNull(etapesJoin.get("dateFin")));
        }

        if (criteria.getNomPartie() != null && !criteria.getNomPartie().isBlank()) {
            Subquery<Long> subquery = countQuery.subquery(Long.class);
            Root<?> implicationRoot = subquery.from(entityManager.getMetamodel().entity("Implication").getJavaType());
            Join<?, ?> partieJoin = implicationRoot.join("partie");
            subquery.select(cb.literal(1L))
                    .where(cb.and(
                            cb.equal(implicationRoot.get("dossier"), countRoot),
                            cb.or(
                                    cb.like(cb.lower(partieJoin.get("nom")), "%" + criteria.getNomPartie().toLowerCase() + "%"),
                                    cb.like(cb.lower(partieJoin.get("prenom")), "%" + criteria.getNomPartie().toLowerCase() + "%")
                            )
                    ));
            countPredicates.add(cb.exists(subquery));
        }

        if (criteria.getDateOuvertureMin() != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("dateOuverture"), criteria.getDateOuvertureMin()));
        }

        if (criteria.getDateOuvertureMax() != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("dateOuverture"), criteria.getDateOuvertureMax()));
        }

        if (criteria.getRisqueFinancierMin() != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("risqueFinancier"), criteria.getRisqueFinancierMin()));
        }

        if (criteria.getRisqueFinancierMax() != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("risqueFinancier"), criteria.getRisqueFinancierMax()));
        }

        if (criteria.getMatriculeJuriste() != null && !criteria.getMatriculeJuriste().isBlank()) {
            Subquery<Long> subquery = countQuery.subquery(Long.class);
            Root<?> djRoot = subquery.from(entityManager.getMetamodel().entity("DossierJuriste").getJavaType());
            Join<?, ?> juristeJoin = djRoot.join("juriste");
            subquery.select(cb.literal(1L))
                    .where(cb.and(
                            cb.equal(djRoot.get("dossier"), countRoot),
                            cb.equal(juristeJoin.get("matricule"), criteria.getMatriculeJuriste())
                    ));
            countPredicates.add(cb.exists(subquery));
        }

        if (criteria.getIdentifiantCabinet() != null && !criteria.getIdentifiantCabinet().isBlank()) {
            Subquery<Long> subquery = countQuery.subquery(Long.class);
            Root<?> dcRoot = subquery.from(entityManager.getMetamodel().entity("DossierCabinet").getJavaType());
            Join<?, ?> cabinetJoin = dcRoot.join("cabinet");
            subquery.select(cb.literal(1L))
                    .where(cb.and(
                            cb.equal(dcRoot.get("dossier"), countRoot),
                            cb.equal(cabinetJoin.get("identifiantCabinet"), criteria.getIdentifiantCabinet())
                    ));
            countPredicates.add(cb.exists(subquery));
        }

        countQuery.select(cb.countDistinct(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    @Transactional
    public void deleteDossier(String numeroDossier) {
        if (!dossierRepository.existsById(numeroDossier)) {
            throw new ResourceNotFoundException("Dossier introuvable avec le numéro " + numeroDossier);
        }
        // Doit precede la suppression des etapes : AudienceDecision reference desormais EtapeDossier.
        audienceDecisionRepository.deleteAll(audienceDecisionRepository.findByEtapeDossier_Dossier_NumeroDossier(numeroDossier));
        documentRepository.deleteAll(documentRepository.findByDossier_NumeroDossier(numeroDossier));
        implicationRepository.deleteAll(implicationRepository.findByDossier_NumeroDossier(numeroDossier));
        dossierJuristeRepository.deleteAll(dossierJuristeRepository.findByDossier_NumeroDossier(numeroDossier));
        dossierCabinetRepository.deleteAll(dossierCabinetRepository.findByDossier_NumeroDossier(numeroDossier));
        etapeDossierRepository.deleteAll(etapeDossierRepository.findByDossier_NumeroDossier(numeroDossier));
        dossierRepository.deleteById(numeroDossier);
        auditService.enregistrer(AuditLog.Action.DELETE, "Dossier", numeroDossier,
                "suppression du dossier et de ses elements rattaches (etapes, audiences, documents, implications, juristes, cabinets)");
    }
}
