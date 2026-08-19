CREATE TABLE TYPE_DOCUMENT (
    code VARCHAR(50) NOT NULL,
    libelle VARCHAR(150) NOT NULL,
    PRIMARY KEY (code)
);

-- Reprise des types jusqu'ici codés en dur côté backend, pour ne rien changer au
-- comportement existant : ils deviennent simplement modifiables via le référentiel.
INSERT INTO TYPE_DOCUMENT (code, libelle) VALUES
    ('requete', 'Requête'),
    ('piece_justificative', 'Pièce justificative'),
    ('pv_audience', 'PV d''audience'),
    ('releve_general_service', 'Relevé général de service'),
    ('indice', 'Indice'),
    ('acte_carriere', 'Acte carrière'),
    ('assignation', 'Assignation'),
    ('convocation', 'Convocation'),
    ('decision_justice', 'Décision de justice');
