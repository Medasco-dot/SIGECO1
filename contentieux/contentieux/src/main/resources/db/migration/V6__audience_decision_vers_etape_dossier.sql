-- A la demande du maitre de stage : une audience/decision se rattache desormais a l'etape
-- precise du dossier durant laquelle elle a eu lieu (EtapeDossier), et non plus directement
-- au dossier dans son ensemble. Cardinalite : AudienceDecision (1,1) -- EtapeDossier (0,N).
--
-- Aucune vraie donnee utilisateur n'existe a ce stade (uniquement des donnees de demonstration
-- creees en developpement) : la migration supprime donc les lignes existantes plutot que de
-- tenter un retro-remplissage vers une etape precise, qui n'aurait pas de sens metier fiable.
ALTER TABLE audience_decision DROP FOREIGN KEY FKhqa04w9df281asnv4nm8pfd2;

DELETE FROM audience_decision;

ALTER TABLE audience_decision DROP COLUMN numero_dossier;

ALTER TABLE audience_decision ADD COLUMN id_etape_dossier INT NOT NULL;

ALTER TABLE audience_decision
    ADD CONSTRAINT fk_audience_decision_etape_dossier
    FOREIGN KEY (id_etape_dossier) REFERENCES etape_dossier(id);
