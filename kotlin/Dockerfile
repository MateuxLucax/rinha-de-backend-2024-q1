FROM eclipse-temurin:21-jammy AS build

RUN apt update && apt install -y wget

WORKDIR /app

COPY . .

RUN wget https://services.gradle.org/distributions/gradle-8.6-bin.zip -O gradle.zip \
    mkdir /opt/gradle \
    unzip -d /opt/gradle gradle.zip \
    export PATH=$PATH:/opt/gradle/gradle-8.6/bin \
    gradle --version \
    ./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true

FROM quay.io/quarkus/quarkus-micro-image:2.0 AS final

WORKDIR /work/

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

COPY --chown=1001:root --from=build /app/build/*-runner /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
