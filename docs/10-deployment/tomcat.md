# External Tomcat 10+ deployment

## Requirements

- Java 17;
- Apache Tomcat 10+ configured to use Java 17;
- reachable PostgreSQL database with permission to apply Flyway migrations.

## Build

```bash
mvn clean package
```

The executable WAR is produced at `target/software-store.war`. It may also be run locally with:

```bash
java -jar target/software-store.war
```

## Deploy to Tomcat

1. Stop Tomcat.
2. Copy `target/software-store.war` to `<CATALINA_BASE>/webapps/`.
3. Set environment variables for the Tomcat process:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/software_store
SPRING_DATASOURCE_USERNAME=software_user
SPRING_DATASOURCE_PASSWORD=replace-db-password
APP_JWT_SECRET=replace-with-a-long-random-secret
SPRING_PROFILES_ACTIVE=default
```

`APP_SEED_ADMIN_EMAIL` and `APP_SEED_ADMIN_PASSWORD` are optional and only relevant when the `dev` profile is intentionally enabled.

4. Start Tomcat using its normal `bin/startup.sh` or `bin/startup.bat` command.

The application URL is `http://localhost:8080/software-store/`. Rename the WAR to `ROOT.war` if it must be served at the context root.
