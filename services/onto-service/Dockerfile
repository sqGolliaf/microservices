# Stage 1: Extract Spring Boot application layers
FROM bellsoft/liberica-openjre-alpine:21-cds AS layers
WORKDIR /application
COPY build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# Stage 2: Final runtime image
FROM bellsoft/liberica-openjre-alpine:21-cds

LABEL maintainer="sqGolliaf"
LABEL description="Ontology Service Microservice"

VOLUME /tmp

# Configure non-root user
RUN adduser -S -D -H -s /sbin/nologin appuser
USER appuser

WORKDIR /application

# Copy Spring Boot application layers
COPY --from=layers --chown=appuser:appuser /application/extracted/dependencies/ ./
COPY --from=layers --chown=appuser:appuser /application/extracted/spring-boot-loader/ ./
COPY --from=layers --chown=appuser:appuser /application/extracted/snapshot-dependencies/ ./
COPY --from=layers --chown=appuser:appuser /application/extracted/application/ ./

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD java -cp /application org.springframework.boot.loader.JarLauncher || exit 1

# JVM options
ENV JAVA_CDS_OPTS="-XX:SharedArchiveFile=app.jsa -Xlog:class+load:file=/tmp/classload.log"
ENV JAVA_ERROR_FILE_OPTS="-XX:ErrorFile=/tmp/java_error.log"
ENV JAVA_HEAP_DUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp"
ENV JAVA_ON_OUT_OF_MEMORY_OPTS="-XX:+ExitOnOutOfMemoryError"
ENV JAVA_NATIVE_MEMORY_TRACKING_OPTS="-XX:NativeMemoryTracking=summary -XX:+UnlockDiagnosticVMOptions -XX:+PrintNMTStatistics"

ENTRYPOINT exec java \
    $JAVA_HEAP_DUMP_OPTS \
    $JAVA_ON_OUT_OF_MEMORY_OPTS \
    $JAVA_ERROR_FILE_OPTS \
    $JAVA_NATIVE_MEMORY_TRACKING_OPTS \
    $JAVA_CDS_OPTS \
    -jar app.jar