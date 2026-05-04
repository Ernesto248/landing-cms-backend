# Backend Setup

This backend targets:

- `Java 21`
- `Spring Boot 3.5.0`
- `Maven 3.9+`

## Recommended setup for this Windows environment

`scoop` is already available in this machine, so the lowest-friction path is:

```powershell
scoop install openjdk21 maven
java -version
mvn -version
```

Then run the backend:

```powershell
cd backend
mvn spring-boot:run
```

## Reduce setup friction for future machines

After Maven works once, generate Maven Wrapper and commit it:

```powershell
cd backend
mvn -N wrapper:wrapper
```

That should create:

- `mvnw`
- `mvnw.cmd`
- `.mvn/wrapper/*`

After that, other developers can use:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

They will still need `Java 21`, but they will not need a global Maven install.

## Alternative if you prefer global installers

With `winget`:

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK -e
winget install Maven.Maven
```

If the Maven package id is not available in your `winget` source, use `scoop install maven` instead.
