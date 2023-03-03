FROM openjdk:17

RUN groupadd devgrp
RUN useradd -g devgrp -m -d /opt/app devuser
USER devuser

RUN mkdir -p /opt/app/lib
RUN mkdir -p /opt/app/var
RUN mkdir -p /opt/app/etc
RUN mkdir -p /opt/app/bin

#COPY ./src/main/docker/bashrc /opt/app/.bashrc
COPY ./target/lib/*.jar /opt/app/lib
COPY ./target/*.jar /opt/app/lib
COPY ./etc/* /opt/app/etc/


ENV APP_CFG=/opt/app/etc/mktdatapublisher.xml
ENV TZ="America/Los_Angeles"


#ENV JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:8000"
ENV JAVA_OPTS="$JAVA_OPTS -XX:+ExitOnOutOfMemoryError"
ENV JAVA_OPTS="$JAVA_OPTS -XshowSettings:vm -Xmx512M -Xms256M"
ENV JAVA_OPTS="$JAVA_OPTS -Dlogback.configurationFile=/opt/app/etc/logback.xml"
ENV JAVA_OPTS="$JAVA_OPTS --add-opens java.base/jdk.internal.misc=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true"

WORKDIR /opt/app
ENTRYPOINT exec java $JAVA_OPTS -cp "/opt/app/lib/*" gcs.toolset.lattools.EntryPoint
    
