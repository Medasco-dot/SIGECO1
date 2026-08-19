-- Piste d'audit immuable : chaque creation/modification/suppression sur les entites
-- metier sensibles est journalisee (qui, quand, quoi). Aucune methode applicative ne
-- permet de modifier ou supprimer une ligne de cette table (cf. AuditLogRepository).
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `horodatage` datetime(3) NOT NULL,
  `identifiant_acteur` varchar(50) NOT NULL,
  `role_acteur` varchar(30) NOT NULL,
  `action` varchar(20) NOT NULL,
  `type_entite` varchar(60) NOT NULL,
  `identifiant_entite` varchar(60) NOT NULL,
  `details` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX `idx_audit_entite` ON `audit_log` (`type_entite`, `identifiant_entite`);
CREATE INDEX `idx_audit_horodatage` ON `audit_log` (`horodatage`);

-- Liste de revocation des jetons JWT : permet d'invalider immediatement un jeton
-- (deconnexion explicite, compte compromis) sans attendre son expiration naturelle.
CREATE TABLE `revoked_token` (
  `jti` varchar(64) NOT NULL,
  `revoque_le` datetime NOT NULL,
  `expire_le` datetime NOT NULL,
  PRIMARY KEY (`jti`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX `idx_revoked_token_expire` ON `revoked_token` (`expire_le`);
