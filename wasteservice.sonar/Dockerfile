FROM openjdk:12.0.2
EXPOSE 8031
ADD ./build/distributions/wasteservice.sonar-1.0.tar  /


WORKDIR /wasteservice.sonar-1.0/bin
COPY ./dockerresources/*.pl ./
COPY ./dockerresources/*.json ./
RUN ls
CMD ["bash", "wasteservice.sonar"]
