# SIGECO — Plateforme de gestion du contentieux CARFO

Application de gestion du contentieux pour le Service Contentieux et Juridique de la CARFO
(Caisse Autonome de Retraite des Fonctionnaires, Burkina Faso) : suivi des dossiers, des
parties impliquées, des juristes internes et cabinets d'avocats-conseils externes, des
étapes de procédure, des audiences et décisions, des documents associés, et des indicateurs
financiers et statistiques.

Projet développé dans le cadre d'un stage académique (voir `Cahier_des_charges_Contentieux_CARFO_v5`
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
export DB_USERNAME=... DB_PASSWORD=... JWT_SECRET=... FIELD_ENCRYPTION_KEY=...
./mvnw spring-boot:run
```

Variables d'environnement principales (voir `application.properties`) :

| Variable | Rôle |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Connexion MySQL |
| `JWT_SECRET` | Secret de signature des jetons d'authentification |
| `FIELD_ENCRYPTION_KEY` | Clé AES-256 (32 octets en base64) de chiffrement des champs sensibles (ex. numéro CNIB) |
| `SERVER_PORT` | Port HTTP (8082 par défaut) |
| `app.test-data.enabled` | Active `/api/test/setup` (jeu de données de démo, désactivé par défaut) |

### Frontend

```bash
cd contentieux-web
npm install
npm start
```

### Docker

Un `docker-compose.yml` est fourni à la racine (MySQL + migrations Flyway + backend + frontend).

```bash
cp .env.example .env
# renseigner DB_ROOT_PASSWORD, DB_PASSWORD, JWT_SECRET, FIELD_ENCRYPTION_KEY dans .env
docker compose up -d
```

Le service `flyway` applique automatiquement les migrations sur une base vide avant le
démarrage du backend. Une fois la stack démarrée :

| Service | URL |
|---|---|
| Frontend (nginx) | http://localhost |
| Backend (API) | http://localhost:8080 |
| MySQL (hôte) | localhost:3307 (mappé sur 3306 dans le réseau interne, pour éviter un conflit avec une éventuelle instance MySQL locale) |

## Rôles et droits

Trois profils, conformes au diagramme de cas d'utilisation : **juriste** (création et suivi
quotidien des dossiers), **chef de service** (assignation des juristes aux dossiers,
supervision, statistiques) et **direction générale** (consultation, statistiques).

## Tests

```bash
cd contentieux/contentieux && ./mvnw test
cd contentieux-web && npm test
cd contentieux-web && npm run test:e2e   # Playwright : parcours e2e, RBAC, responsive, accessibilité (axe-core)
```
