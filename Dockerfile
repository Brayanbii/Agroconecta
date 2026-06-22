# =============================================
# Stage 1: Build con Maven + JDK 17
# =============================================
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /build

# Utilidades necesarias para Maven wrapper (corrige CRLF de Windows)
RUN apk add --no-cache dos2unix

# Copiar wrapper de Maven y pom.xml primero (caché de dependencias)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Corregir saltos de línea Windows y hacer ejecutable
RUN dos2unix mvnw 2>/dev/null; chmod +x mvnw

# Descargar TODAS las dependencias (esta capa se cachea a menos que cambie pom.xml)
RUN ./mvnw dependency:go-offline -B -q

# Copiar código fuente
COPY src/ src/

# Compilar y empaquetar (sin tests, build más rápido)
RUN ./mvnw package -DskipTests -B -q

# =============================================
# Stage 2: Runtime con JRE 17 Alpine (imagen mínima)
# =============================================
FROM eclipse-temurin:17-jre-alpine

# curl, tzdata (SIN Python - ahorra ~80 MB RAM)
RUN apk add --no-cache curl tzdata

# Zona horaria Colombia
RUN cp /usr/share/zoneinfo/America/Bogota /etc/localtime && \
    echo "America/Bogota" > /etc/timezone

# Usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar JAR compilado desde la etapa de build
COPY --from=build /build/target/*.jar app.jar

# Copiar certificado CA de Aiven desde los recursos del proyecto
COPY src/main/resources/certs/aiven-ca.pem /tmp/aiven-ca.pem

# Importar certificado Aiven en el truststore de Java (necesario para SSL con MySQL)
# changeit = password por defecto del cacerts de Java
RUN keytool -importcert -trustcacerts -alias aiven-mysql-ca \
    -file /tmp/aiven-ca.pem \
    -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt && \
    rm /tmp/aiven-ca.pem

# Crear directorio para uploads (NO se usa en prod con MongoDB, pero queda como fallback)
RUN mkdir -p /app/images && chown -R appuser:appgroup /app

# Cambiar a usuario no-root
USER appuser

# Puerto que Render asigna vía $PORT (default 8080)
EXPOSE 8080

# Health check ligero: verifica que el proceso Java siga vivo
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
    CMD pgrep -f "app.jar" || exit 1

# JVM ultra-ajustada para 512 MB - JAVA_OPTS primero para que no sobre-escriba
ENTRYPOINT ["sh", "-c", "java \
    $JAVA_OPTS \
    -XX:+UseSerialGC \
    -Xmx128m \
    -Xmn32m \
    -Xss192k \
    -XX:MaxDirectMemorySize=16m \
    -XX:+DisableExplicitGC \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=America/Bogota \
    -jar app.jar"]
