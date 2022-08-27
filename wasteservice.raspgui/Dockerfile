FROM openjdk:12.0.2
EXPOSE 8096
ADD ./build/distributions/wasteservice.raspgui-boot.tar /


WORKDIR /wasteservice.raspgui-boot/bin
COPY ./dockerresources/*.json ./
COPY ./dockerresources/*.pl ./
RUN ls
CMD ["bash", "wasteservice.raspgui"]
