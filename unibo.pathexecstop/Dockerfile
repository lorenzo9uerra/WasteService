FROM openjdk:12.0.2
EXPOSE 8025
ADD ./build/distributions/unibo.pathexecstop-1.0.tar  /


WORKDIR /unibo.pathexecstop-1.0/bin
COPY ./dockerresources/*.pl ./
COPY ./dockerresources/*.json ./
RUN ls
CMD ["bash", "unibo.pathexecstop"]
