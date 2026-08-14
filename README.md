# CARFO — Plateforme de gestion du contentieux

Application de gestion du contentieux pour le Service Contentieux et Juridique de la CARFO
(Caisse Autonome de Retraite des Fonctionnaires, Burkina Faso) : suivi des dossiers, des
parties impliquées, des juristes internes et cabinets d'avocats-conseils externes, des
étapes de procédure, des audiences et décisions, des documents associés, et des indicateurs
financiers et statistiques.

Projet développé dans le cadre d'un stage académique (voir `Cahier_des_charges_Contentieux_CARFO_v3`
pour le périmètre fonctionnel détaillé).

## Stack technique

- **Backend** : Java 17, Spring Boot, Spring Security (JWT), Spring Data JPA, MySQL
- **Frontend** : Angular (standalone components, signals)
- **Documentation API** : springdoc-openapi / Swagger UI
- **Exports** : Apache POI (Word/Excel), Apache PDFBox (PDF)

## Démarrage local

### Backend

```bash
cd contentieux/contentieux
export DB_USERNAME=... DB_PASSWORD=... JWT_SECRET=...
./mvnw spring-boot:run
```

Variables d'environnement principales (voir `application.properties`) :

| Variable | Rôle |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Connexion MySQL |
| `JWT_SECRET` | Secret de signature des jetons d'authentification |
| `SERVER_PORT` | Port HTTP (8082 par défaut) |
| `app.test-data.enabled` | Active `/api/test/setup` (jeu de données de démo, désactivé par défaut) |

### Frontend

```bash
cd contentieux-web
npm install
npm start
```

### Docker

Un `docker-compose.yml` est fourni à la racine (MySQL + backend + frontend) ; ajuster les
variables d'environnement avant utilisation.

## Rôles et droits

Trois profils, conformes au diagramme de cas d'utilisation : **juriste** (création et suivi
quotidien des dossiers), **chef de service** (assignation des juristes aux dossiers,
supervision, statistiques) et **direction générale** (consultation, statistiques).

## Tests

```bash
cd contentieux/contentieux && ./mvnw test
cd contentieux-web && npm test
```
