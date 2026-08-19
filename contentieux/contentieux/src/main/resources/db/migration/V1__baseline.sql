-- Ligne de base Flyway : reprend l'état du schéma tel qu'il existait avant l'adoption des
-- migrations versionnées (créé initialement à la main via des scripts SQL manuels, cf.
-- historique du projet). A partir de V2, toute évolution de schéma doit passer par une
-- nouvelle migration versionnée plutôt que par une modification manuelle de la base.

CREATE TABLE `cabinet` (
  `identifiant_cabinet` varchar(15) NOT NULL,
  `adresse` varchar(100) DEFAULT NULL,
  `mail` varchar(100) DEFAULT NULL,
  `nom_cabinet` varchar(100) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`identifiant_cabinet`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `juriste` (
  `matricule` varchar(15) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenoms` varchar(50) NOT NULL,
  `specialite` enum('acte_carriere','marche_public','penal','pension_retraite','pension_reversement','polyvalent') DEFAULT NULL,
  PRIMARY KEY (`matricule`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `partie` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) NOT NULL,
  `numero_cnib` varchar(20) DEFAULT NULL,
  `prenom` varchar(50) NOT NULL,
  `statut_matrimonial` enum('celibataire','divorce','marie','veuf') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `type_contentieux` (
  `num_contentieux` int NOT NULL AUTO_INCREMENT,
  `nature` enum('acte_carriere','autre','marche_public','penal','pension_retraite','pension_reversement') DEFAULT NULL,
  PRIMARY KEY (`num_contentieux`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dossier` (
  `numero_dossier` varchar(15) NOT NULL,
  `date_ouverture` date NOT NULL,
  `frais_justice` decimal(18,2) DEFAULT NULL,
  `montant_reclame` decimal(18,2) DEFAULT NULL,
  `observation` varchar(2000) DEFAULT NULL,
  `resume_affaire` varchar(2000) DEFAULT NULL,
  `risque_financier` decimal(18,2) DEFAULT NULL,
  `num_contentieux` int NOT NULL,
  PRIMARY KEY (`numero_dossier`),
  KEY `FK8itjcuh5jpuhyajroddsh16mx` (`num_contentieux`),
  CONSTRAINT `FK8itjcuh5jpuhyajroddsh16mx` FOREIGN KEY (`num_contentieux`) REFERENCES `type_contentieux` (`num_contentieux`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `audience_decision` (
  `num_audience_decision` int NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `frais_justice` decimal(38,2) DEFAULT NULL,
  `issue_pour_carfo` enum('defavorable','favorable','partiellement_favorable') DEFAULT NULL,
  `lieu_audience` varchar(100) DEFAULT NULL,
  `montant_du` decimal(38,2) DEFAULT NULL,
  `montant_obtenu` decimal(38,2) DEFAULT NULL,
  `nature_decision` enum('arret','jugement','ordonnance') DEFAULT NULL,
  `resume_decision` varchar(255) DEFAULT NULL,
  `type_etape` enum('appel','cassation','premiere_instance') NOT NULL,
  `numero_dossier` varchar(15) NOT NULL,
  PRIMARY KEY (`num_audience_decision`),
  KEY `FKhqa04w9df281asnv4nm8pfd2` (`numero_dossier`),
  CONSTRAINT `FKhqa04w9df281asnv4nm8pfd2` FOREIGN KEY (`numero_dossier`) REFERENCES `dossier` (`numero_dossier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `document` (
  `id_document` int NOT NULL AUTO_INCREMENT,
  `date_ajout` date DEFAULT NULL,
  `fichier` varchar(255) DEFAULT NULL,
  `type_document` varchar(120) DEFAULT NULL,
  `numero_dossier` varchar(15) NOT NULL,
  PRIMARY KEY (`id_document`),
  KEY `FKg8vc6y6jjnemdna3cwi5o4uac` (`numero_dossier`),
  CONSTRAINT `FKg8vc6y6jjnemdna3cwi5o4uac` FOREIGN KEY (`numero_dossier`) REFERENCES `dossier` (`numero_dossier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dossier_cabinet` (
  `nom_avocat_referent` varchar(255) DEFAULT NULL,
  `identifiant_cabinet` varchar(255) NOT NULL,
  `numero_dossier` varchar(255) NOT NULL,
  PRIMARY KEY (`identifiant_cabinet`,`numero_dossier`),
  KEY `FK2gwi7la64h6yli05w8hakd6wm` (`numero_dossier`),
  CONSTRAINT `FK2gwi7la64h6yli05w8hakd6wm` FOREIGN KEY (`numero_dossier`) REFERENCES `dossier` (`numero_dossier`),
  CONSTRAINT `FKin0fhwykxpjak7qwr4nt8kuxg` FOREIGN KEY (`identifiant_cabinet`) REFERENCES `cabinet` (`identifiant_cabinet`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dossier_juriste` (
  `numero_dossier` varchar(255) NOT NULL,
  `matricule` varchar(255) NOT NULL,
  PRIMARY KEY (`matricule`,`numero_dossier`),
  KEY `FKt7tngykegfqqb70cxqxrlmbrc` (`numero_dossier`),
  CONSTRAINT `FKcx5644i9svw6c818jsqtakvk5` FOREIGN KEY (`matricule`) REFERENCES `juriste` (`matricule`),
  CONSTRAINT `FKt7tngykegfqqb70cxqxrlmbrc` FOREIGN KEY (`numero_dossier`) REFERENCES `dossier` (`numero_dossier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `etape_dossier` (
  `id` int NOT NULL AUTO_INCREMENT,
  `date_debut` date NOT NULL,
  `date_fin` date DEFAULT NULL,
  `etape` enum('classe_sans_suite','cloture','en_appel','en_cassation','en_instruction','juge','ouvert') NOT NULL,
  `numero_dossier` varchar(15) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlbkithk8xr600th4pyd4dnrvd` (`numero_dossier`),
  CONSTRAINT `FKlbkithk8xr600th4pyd4dnrvd` FOREIGN KEY (`numero_dossier`) REFERENCES `dossier` (`numero_dossier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `implication` (
  `lien_parente` enum('assure','autre_ayant_droit','enfant','epoux_epouse') DEFAULT NULL,
  `role` enum('defendeur','demandeur') DEFAULT NULL,
  `numero_dossier` varchar(255) NOT NULL,
  `id_partie` int NOT NULL,
  PRIMARY KEY (`id_partie`,`numero_dossier`),
  KEY `FK6x36vxdnx1rq6aprnj8m1ik6n` (`numero_dossier`),
  CONSTRAINT `FK6x36vxdnx1rq6aprnj8m1ik6n` FOREIGN KEY (`numero_dossier`) REFERENCES `dossier` (`numero_dossier`),
  CONSTRAINT `FKji5mruxgkeeis5ajsdqhb03lc` FOREIGN KEY (`id_partie`) REFERENCES `partie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `utilisateur` (
  `id` int NOT NULL AUTO_INCREMENT,
  `identifiant` varchar(50) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `role` enum('chef_service','direction_generale','juriste') NOT NULL,
  `matricule_juriste` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKo3vqges7u1b1bcp7k9fh5mp8o` (`identifiant`),
  KEY `fk_utilisateur_juriste` (`matricule_juriste`),
  CONSTRAINT `fk_utilisateur_juriste` FOREIGN KEY (`matricule_juriste`) REFERENCES `juriste` (`matricule`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
