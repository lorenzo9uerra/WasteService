FROM openjdk:12.0.2
EXPOSE 8080
EXPOSE 8023
ADD ./build/distributions/wasteservice.core-1.0.tar  /
ADD ./build/distributions/wasteservice.core-boot-1.0.tar /


WORKDIR /wasteservice.core-boot-1.0/bin
COPY ./dockerresources/sysRules.pl ./
COPY ./dockerresources/wasteservice_wasteservice.pl ./wasteservice.pl
COPY ./dockerresources/*.json ./
RUN ls
CMD ["bash", "wasteservice.core"]
