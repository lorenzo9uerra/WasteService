FROM openjdk:12.0.2
EXPOSE 8080
ADD ./build/distributions/wasteservice-1.0.tar  /
ADD ./build/distributions/wasteservice-boot-1.0.tar /


WORKDIR /wasteservice-boot-1.0/bin
COPY ./*.pl ./
COPY ./*.json ./
RUN ls

CMD ["bash", "wasteservice"]
