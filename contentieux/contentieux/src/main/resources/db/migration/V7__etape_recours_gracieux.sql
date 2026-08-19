-- A la demande du maitre de stage : le recours gracieux devient une etape reelle du systeme
-- (il etait jusqu'ici explicitement hors perimetre, cf. cahier des charges v4 S7 -- desormais
-- retire de cette liste en v5).
ALTER TABLE etape_dossier MODIFY COLUMN etape
    ENUM('recours_gracieux','ouvert','en_instruction','juge','en_appel','en_cassation','cloture','classe_sans_suite')
    NOT NULL;
