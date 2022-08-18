FROM openjdk:12.0.2
EXPOSE 8095
ADD ./build/distributions/wasteservice.statusgui-1.0.tar  /
ADD ./build/distributions/wasteservice.statusgui-boot-1.0.tar /


WORKDIR /wasteservice.statusgui-boot-1.0/bin
COPY ./dockerresources/*.json ./
RUN ls
CMD ["bash", "wasteservice.statusgui"]
