# Settings.
ARG USER_ID=3001
ARG USER_NAME=catalog_user
ARG DIR_HOME=/home/$USER_NAME/
ARG DIR_SERVE=$DIR_HOME/serve/
ARG DIR_SOURCE=/source/
ARG BUILD_JAR="false"
ARG JAVA_VERSION=11
ARG MAVEN_ARGS="clean package -Pjar -DskipTests -Dcheckstyle.skip=true -Dasciidoctor.skip=true -Djacoco.skip=true"
ARG PATH_JAR=service/target/ROOT.jar
ARG UPDATE_MAVEN="true"
ARG UPDATE_SERVE="true"

# Maven stage.
FROM maven:3-eclipse-temurin-${JAVA_VERSION}-alpine AS maven
ARG BUILD_JAR
ARG DIR_SOURCE
ARG MAVEN_ARGS
ARG PATH_JAR
ARG UPDATE_MAVEN


# Switch to the source directory.
WORKDIR ${DIR_SOURCE}

# Copy files over.
COPY ./pom.xml ./pom.xml
COPY ./domain ./domain
COPY ./service ./service

# Conditionally update system.
RUN \
  if [[ "${UPDATE_MAVEN}" == "true" ]] ; then \
    apk -U upgrade --no-cache -a --prune ; \
  fi

# Conditionally build jar file or designate that the pre-built jar file is copied over.
RUN \
  if [[ "${BUILD_JAR}" == "true" ]] ; then \
    echo "Building JAR file with command: mvn ${MAVEN_ARGS}." ; \
    mvn ${MAVEN_ARGS} ; \
  else \
    echo "Using JAR file from: ${DIR_SOURCE}${PATH_JAR}." ; \
  fi

# Ensure the JAR file can be accessed from other stage.
RUN chmod -R ugo+rX ${DIR_SOURCE}


# Start the server from the JAR file.
FROM eclipse-temurin:${JAVA_VERSION}-alpine as serve
ARG DIR_HOME
ARG DIR_SERVE
ARG DIR_SOURCE
ARG PATH_JAR
ARG UPDATE_SERVE
ARG USER_ID
ARG USER_NAME

# Conditionally update system.
RUN \
  if [[ "${UPDATE_SERVE}" == "true" ]] ; then \
    apk -U upgrade --no-cache -a --prune ; \
  fi

# Create the group (use a high ID to attempt to avoid potential conflicts).
RUN addgroup -g ${USER_ID} ${USER_NAME}

# Create the user (use a high ID to attempt to avoid potential conflicts).
RUN adduser -h ${DIR_HOME} -u ${USER_ID} -G ${USER_NAME} -D ${USER_NAME}

# Create log directory.
RUN mkdir -p ${DIR_SERVE}logs

# Give user access to the log directory.
RUN chown ${USER_ID}:${USER_ID} ${DIR_SERVE}logs

# Login as user.
USER ${USER_NAME}

# Switch to the serve directory.
WORKDIR ${DIR_SERVE}

# Copy over the built artifact from the maven image.
COPY --from=maven ${DIR_SOURCE}${PATH_JAR} ./catalog-service.jar

# Start the server.
CMD [ "java", "-jar", "./catalog-service.jar" ]
