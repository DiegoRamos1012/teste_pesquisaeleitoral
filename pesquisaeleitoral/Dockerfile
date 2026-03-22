# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /build

# Copiar apenas pom.xml e .mvn para cache das dependencias
COPY pom.xml .
COPY .mvn/ .mvn/

# Baixar dependencias (cache)
RUN mvn dependency:resolve dependency:resolve-plugins -q

# Copiar codigo-fonte
COPY src/ src/

# Compilar JAR
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copiar JAR compilado da stage anterior
COPY --from=builder /build/target/pesquisaeleitoral-*.jar app.jar

# Criar usuario nao-root por seguranca
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Variaveis de ambiente para banco (pode ser sobrescrita em docker run/compose)
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_NAME=pesquisaeleitoral
ENV DB_USER=postgres
ENV SPRING_PROFILES_ACTIVE=local

# Porta da API
EXPOSE 8080

# Health check basico (verifica Swagger, endpoint que sempre existe)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/swagger-ui.html || exit 1

# Comando de inicializacao
ENTRYPOINT ["java", "-jar", "app.jar"]


