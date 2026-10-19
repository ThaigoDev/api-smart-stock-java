# ===== BUILD =====
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src

# as chaves que assinam os tokens JWT não são versionadas (.gitignore); se não vierem no contexto
# do build, gera um par RSA próprio da imagem para que ela funcione a partir de um clone limpo
RUN if [ ! -f src/main/resources/keys/app.key ]; then \
      mkdir -p src/main/resources/keys && \
      openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out src/main/resources/keys/app.key && \
      openssl rsa -in src/main/resources/keys/app.key -pubout -out src/main/resources/keys/app.pub; \
    fi

RUN mvn -q clean package -DskipTests


# ===== RUN =====
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# Configuração por variáveis de ambiente (ver application.yaml):
#   DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD, ADMIN_EMAIL, ADMIN_SENHA,
#   SEED_ENABLED, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET e, no perfil production, PUBLIC_KEY e PRIVATE_KEY.
# Nenhuma é declarada vazia aqui para não anular os valores padrão.

ENTRYPOINT ["java", "-jar", "app.jar"]
