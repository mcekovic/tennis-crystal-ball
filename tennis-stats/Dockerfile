FROM adoptopenjdk/openjdk11:alpine-jre
EXPOSE 8080
ARG DIST=/dist
ARG APP=/tennis-stats
COPY build/distributions/tennis-stats-*.tar ${DIST}/
RUN cd ${DIST} && tar xvf tennis-stats-*.tar && rm -f tennis-stats-*.tar && mv tennis-stats-* ${APP} && cd .. && rm -rf ${DIST}
WORKDIR ${APP}
HEALTHCHECK --interval=1m --timeout=15s --start-period=30s --retries=3\
  CMD wget --quiet --tries=1 --output-document - http://localhost:8080/actuator/health | grep -q '^{"status":"UP"' && exit 0 || exit 1
ENTRYPOINT ["java", "-server", "-cp", "lib/*:lib/GeoLite2-Country.zip",\
 "-Xms256m", "-Xmx256m",\
 "org.strangeforest.tcb.stats.TennisStatsApplication"]