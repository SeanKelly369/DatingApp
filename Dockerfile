FROM ubuntu
RUN groupadd -r ronwhite && useradd -r -s /bin/false -g ronwhite ronwhite
WORKDIR /
COPY . /
RUN chown -R ronwhite:ronwhite /usersystem
USER ronwhite
CMD tail -f /dev/null

FROM maven:3.6.3-jdk-11 AS build
RUN mkdir /usr/src/usersystem
COPY . /usr/src/usersystem
WORKDIR /usr/src/usersystem
RUN mvn clean package -DskipTests

FROM openjdk:11.0.10
RUN mkdir /usersystem
COPY --from=build /usr/src/usersystem/target/usersystem-0.0.1-SNAPSHOT.jar /usersystem/
EXPOSE 8082
WORKDIR /usersystem

CMD java -jar usersystem-0.0.1-SNAPSHOT.jar
