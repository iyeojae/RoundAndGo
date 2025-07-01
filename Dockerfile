# STEP 1: Use JDK image
FROM eclipse-temurin:21-jdk-alpine

# STEP 2: Set workdir
WORKDIR /app

# STEP 3: Copy build artifact
COPY build/libs/RoundAndGo-0.0.1-SNAPSHOT.jar app.jar

# STEP 4: Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]