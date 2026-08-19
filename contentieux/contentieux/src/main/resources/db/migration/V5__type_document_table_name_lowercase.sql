-- V4 a créé la table en majuscules (TYPE_DOCUMENT), incohérent avec la convention du reste du
-- schéma (toutes les autres tables sont en minuscules) et avec la stratégie de nommage par
-- défaut de Spring Boot, qui résout toujours les noms de table en minuscules : provoque une
-- erreur de validation de schéma sur MySQL Linux (sensible à la casse, utilisé en conteneur
-- Docker). V4 reste inchangée (les migrations Flyway déjà appliquées ne doivent jamais être
-- modifiées) ; ce correctif passe par une nouvelle migration.
-- Renommage en deux temps via un nom intermédiaire : un RENAME direct TYPE_DOCUMENT ->
-- type_document echoue avec "Table already exists" sur MySQL Windows, ou lower_case_table_names
-- resout les deux noms vers le meme identifiant (le renommage se heurte alors a lui-meme).
RENAME TABLE TYPE_DOCUMENT TO type_document_tmp_rename;
RENAME TABLE type_document_tmp_rename TO type_document;
