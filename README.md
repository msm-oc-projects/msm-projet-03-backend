# ChâTop — back-end

API REST sécurisée du portail de locations ChâTop, réalisée avec Java et
Spring Boot. Le back-end respecte le contrat du starter Angular fourni par
OpenClassrooms.

## Technologies

- Java 17 ;
- Spring Boot 3.5.16 ;
- Spring Web, Spring Data JPA et Spring Security ;
- authentification JWT stateless avec le support Resource Server ;
- MySQL 8.4 et H2 uniquement pour les tests ;
- OpenAPI 3 et Swagger UI.

## Prérequis

- JDK 17 ;
- Maven 3.9 ou le Maven fourni par l'IDE ;
- Docker avec le plugin Docker Compose ;
- ports `3001`, `3306` et `8081` disponibles, ou adaptés dans `.env`.

Node.js et Angular CLI ne sont nécessaires que pour tester le starter
front-end séparé.

## Installation et lancement

Depuis le dossier du back-end :

```bash
cp .env.example .env
```

Remplacer dans `.env` les mots de passe MySQL de démonstration et renseigner
un secret JWT Base64 d'au moins 256 bits. Il peut être généré ainsi :

```bash
openssl rand -base64 48
```

Le fichier `.env` est ignoré par Git. Démarrer ensuite la base et l'API :

```bash
docker compose up -d
set -a
source .env
set +a
mvn spring-boot:run
```

L'API écoute sur <http://localhost:3001>.

### Variables d'environnement

| Variable | Rôle | Valeur par défaut |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | administration locale du conteneur | obligatoire |
| `MYSQL_PASSWORD` | mot de passe de `chatop_app` et de l'API | obligatoire |
| `JWT_SECRET` | clé HMAC Base64 d'au moins 256 bits | obligatoire |
| `JWT_ISSUER` | émetteur attendu dans le JWT | `chatop-api` |
| `JWT_EXPIRATION` | durée de validité ISO-8601 | `PT24H` |
| `MYSQL_PORT` | port MySQL local | `3306` |
| `PHPMYADMIN_PORT` | port de phpMyAdmin | `8081` |
| `SERVER_PORT` | port HTTP de Spring Boot | `3001` |
| `PUBLIC_BASE_URL` | origine des URL d'images | `http://localhost:3001` |
| `UPLOAD_DIR` | dossier local des images | `uploads` |
| `MAX_IMAGE_SIZE` | taille maximale d'une image | `5MB` |
| `CORS_ALLOWED_ORIGINS` | origines front autorisées, séparées par des virgules | ports Angular locaux |

## Base de données locale

L'environnement Docker fournit :

- MySQL 8.4 LTS sur `127.0.0.1:3306` ;
- phpMyAdmin sur <http://127.0.0.1:8081> ;
- la base `chatop_db` ;
- le compte applicatif `chatop_app`, limité à `SELECT`, `INSERT`, `UPDATE`
  et `DELETE` ;
- les tables `users`, `rentals` et `messages`, initialisées au premier
  démarrage.

Ouvrir ensuite phpMyAdmin et utiliser :

- serveur : `mysql` ;
- utilisateur : `chatop_app` ;
- mot de passe : valeur de `MYSQL_PASSWORD` dans `.env`.

Le compte `root` n'est pas destiné à l'application ni à la démonstration
courante.

### Contrôles

```bash
docker compose exec mysql mysql \
  -u chatop_app -p chatop_db \
  -e "SHOW TABLES;"
```

La commande demande le mot de passe sans l'afficher dans l'historique du
terminal. Le résultat attendu contient `users`, `rentals` et `messages`.

### Arrêt

```bash
docker compose down
```

Les données restent dans le volume nommé `chatop_mysql_data`.

> `docker compose down -v` supprime définitivement le volume et toutes les
> données locales. Ne l'utiliser que pour réinitialiser volontairement la base.

## API Spring Boot

Le port par défaut `3001` correspond à la cible déjà définie dans le proxy
Angular.

Si Maven signale un `JAVA_HOME` incorrect à cause d'une configuration globale
du poste, indiquer explicitement le chemin du JDK 17. Sur le poste de
validation actuel :

```bash
MAVEN_SKIP_RC=1 JAVA_HOME=/usr/lib/jvm/java-17-openjdk mvn spring-boot:run
```

### Routes disponibles

| Méthode | Route | Accès | Réponse principale |
|---|---|---|---|
| `POST` | `/api/auth/register` | public | `{ "token": "..." }` |
| `POST` | `/api/auth/login` | public | `{ "token": "..." }` |
| `GET` | `/api/auth/me` | JWT | utilisateur courant sans mot de passe |
| `GET` | `/api/user/{id}` | JWT | utilisateur demandé sans mot de passe |
| `GET` | `/api/rentals` | JWT | `{ "rentals": [...] }` |
| `GET` | `/api/rentals/{id}` | JWT | location demandée |
| `POST` | `/api/rentals` | JWT | création multipart d'une location |
| `PUT` | `/api/rentals/{id}` | JWT propriétaire | modification multipart d'une location |
| `POST` | `/api/messages` | JWT | envoi d'un message |

Les images créées sont enregistrées hors de la base dans `uploads/` et leur
URL publique est conservée dans la colonne `picture`. `GET /uploads/**` reste
public afin que les balises `<img>` du front puissent afficher les fichiers ;
toutes les routes REST métier sont protégées, sauf `register` et `login`.

## Documentation OpenAPI et Swagger

La documentation reste publique :

- Swagger UI : <http://localhost:3001/swagger-ui.html> ;
- contrat OpenAPI JSON : <http://localhost:3001/v3/api-docs>.

Pour essayer une route protégée dans Swagger UI :

1. appeler `POST /api/auth/register` ou `POST /api/auth/login` ;
2. copier uniquement la valeur du champ `token` ;
3. cliquer sur **Authorize** et coller cette valeur ;
4. exécuter les routes protégées. Swagger ajoute lui-même le préfixe
   `Bearer`.

Chaque opération documente son corps ou ses paramètres, sa réponse nominale
et les statuts d'erreur possibles. Les routes sont regroupées sous les tags
Authentification, Utilisateurs, Locations et Messages.

### Tests automatisés

```bash
mvn verify
```

Les 12 tests d'intégration utilisent une base H2 isolée et couvrent les succès
et erreurs de toutes les routes, le hash BCrypt, l'utilisation réelle des JWT,
les droits du propriétaire, la protection contre l'usurpation du `user_id`,
le stockage des images et l'accès public à OpenAPI.

## Organisation du code

```text
src/main/java/com/chatop/api/
├── auth/       # inscription, connexion et JWT
├── user/       # profils utilisateurs
├── rental/     # locations et règles de propriété
├── message/    # messages liés aux locations
├── security/   # sécurité stateless et validation JWT
├── storage/    # validation et stockage des images
├── config/     # CORS, ressources statiques et OpenAPI
└── shared/     # réponses et gestion d'erreurs communes
```

Chaque domaine sépare contrôleur REST, service métier, DTO, entité JPA et
repository. Les entités ne sont jamais exposées directement par l'API.

## Test avec Angular

1. arrêter Mockoon afin de libérer le port 3001 ;
2. démarrer l'API Spring Boot ;
3. dans le starter Angular, lancer `npm start` ;
4. ouvrir <http://localhost:4200> ;
5. créer un compte ou se connecter ;
6. créer une location avec une image, l'ouvrir puis la modifier ;
7. avec un second compte, envoyer un message au propriétaire.

Le front enregistre le JWT reçu et l'envoie dans l'en-tête
`Authorization: Bearer <token>`. Le propriétaire d'une location est déterminé
par ce JWT, tout comme l'auteur réel d'un message : les identifiants fournis
par le client ne peuvent donc pas servir à usurper un autre compte.
