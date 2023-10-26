FROM bellsoft/liberica-openjdk-alpine:17

RUN apk add tzdata gettext && rm -rf /var/cache/apk/*

ENV CRON_CMD='java -cp /app/BOOT-INF/classes/:/app/BOOT-INF/lib/* ru.hostco.alrosa.preliminarymedexamtohrtransmitter.PreliminaryMedExamToHrTransmitterApplication'
ENV CRON_SCHEDULE='0 7 * * *'
ENV CRON_COMMAND="if jps | grep "PreliminaryMedExamToHrTransmitterApplication" ; then echo "Skip" > /proc/1/fd/1 2>/proc/1/fd/2 ; else $CRON_CMD > /proc/1/fd/1 2>/proc/1/fd/2 ; fi"

WORKDIR /app

RUN mkdir /app/logs

VOLUME /app/logs

COPY target/. .

ENTRYPOINT echo "$CRON_SCHEDULE" "$CRON_COMMAND" > /var/spool/cron/crontabs/root && crond -f
