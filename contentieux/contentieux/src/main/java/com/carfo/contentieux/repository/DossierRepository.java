package com.carfo.contentieux.repository;

import com.carfo.contentieux.model.Dossier;
import com.carfo.contentieux.model.EtapeDossier;
import com.carfo.contentieux.model.TypeContentieux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, String> {
    List<Dossier> findByNumeroDossierContainingIgnoreCase(String numeroDossier);
    List<Dossier> findByTypeContentieux_Nature(TypeContentieux.Nature nature);
    List<Dossier> findDistinctByEtapes_Etape(EtapeDossier.Etape etape);
    List<Dossier> findDistinctByEtapes_DateFinIsNullAndEtapes_Etape(EtapeDossier.Etape etape);

    @Query("""
            select distinct d from Dossier d
            join Implication i on i.dossier = d
            join i.partie p
            where lower(p.nom) like lower(concat('%', :nomPartie, '%'))
               or lower(p.prenom) like lower(concat('%', :nomPartie, '%'))
            """)
    List<Dossier> findByPartie(@Param("nomPartie") String nomPartie);
}
