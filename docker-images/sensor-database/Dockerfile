FROM mariadb:latest

ENV MYSQL_ROOT_PASSWORD=password
ENV MYSQL_DATABASE=IIOT_ANALYTICS
ENV MYSQL_USER=orbit
ENV MYSQL_PASSWORD=orbit

RUN mkdir -p /tmp/psql_data/

COPY schemas/sensor_synopsis.sql /docker-entrypoint-initdb.d/
