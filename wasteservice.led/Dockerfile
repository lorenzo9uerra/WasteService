FROM openjdk:12.0.2
EXPOSE 8030
ADD ./build/distributions/wasteservice.led-1.0.tar  /

# Led mock
WORKDIR /wasteservice.led-1.0/bin
COPY ./dockerresources/SystemConfig.json ./
COPY ./dockerresources/LedConfiguration.mock.json ./LedConfiguration.json
RUN ls
CMD ["bash", "wasteservice.led"]
