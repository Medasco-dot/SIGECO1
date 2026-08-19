-- Le numero CNIB (piece d'identite nationale) est desormais chiffre au niveau applicatif
-- (AES-GCM) avant stockage : la colonne doit accueillir le texte chiffre encode en base64,
-- plus long que le numero en clair.
ALTER TABLE `partie` MODIFY COLUMN `numero_cnib` varchar(255) DEFAULT NULL;
