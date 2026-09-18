# Settings.
ARG USER_NAME=catalog
ARG SOURCE_DIR=/$USER_NAME/source

# Maven stage.
FROM eclipse-temurin:11-jdk-noble AS maven
ARG USER_NAME
ARG SOURCE_DIR

# Create the user and group (use a high ID to attempt to avoid conflicts).
RUN useradd -d /$USER_NAME -m $USER_NAME

# Update the system and install dependencies.
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Make sure source directory exists.
RUN mkdir -p $SOURCE_DIR && \
    chown -R $USER_NAME:$USER_NAME $SOURCE_DIR

# Set deployment directory.
WORKDIR $SOURCE_DIR

# Copy files over.
COPY ./pom.xml ./pom.xml
COPY ./domain ./domain
COPY ./service ./service

# Assign file permissions.
RUN chown -R ${USER_NAME}:${USER_NAME} ${SOURCE_DIR}

# Login as user.
USER $USER_NAME

# Build.
RUN mvn package -Pjar -DskipTests=true -Dasciidoctor.skip=true -Djacoco.skip=true

# Switch to Normal JRE Stage.
FROM eclipse-temurin:11-jre-alpine
ARG USER_NAME
ARG SOURCE_DIR=/$USER_NAME/source
ARG USER_ID=1000

RUN apk upgrade --no-cache

# Run directly as the numeric UID/GID (No adduser needed!)
USER $USER_ID:$USER_ID

# Set deployment directory.
WORKDIR /app

# Copy over the built artifact and ensure correct numeric ownership
COPY --chown=1000:1000 --from=maven $SOURCE_DIR/service/target/ROOT.jar ./catalog-service.jar

# Run java command.
CMD ["java", "-jar", "./catalog-service.jar"]
