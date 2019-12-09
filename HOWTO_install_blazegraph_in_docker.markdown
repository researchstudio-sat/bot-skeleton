# HOW TO GET BLAZEGRAPH RUNNING IN DOCKER ON DEBIAN 10/BUSTER



## DOWNLOAD BLAZEGRAPH

https://blazegraph.com/ green button, note version from url when hovering over it

## INSTALL DOCKER:

using this guide:
https://docs.docker.com/install/linux/docker-ce/debian/#install-docker-ce

## BUILD DOCKER CONTAINER


`$ su`

`# mkdir ~/docker/build/blazegraph`

`# cd ~/docker/build/blazegraph`

`# mv <file-downloaded-from-blazegraph> ~/docker/build/blazegraph/`

`# nano Dockerfile`

Content:
```DOCKERFILE
FROM openjdk:8-jdk

EXPOSE 9999

COPY blazegraph.jar /usr/local/blazegraph.jar

CMD java -jar /usr/local/blazegraph.jar
```

**Build image**

`# docker image build -t blazegraph:<version-noted-at-download> .`

**Run image**

`# docker container run --publish 9999:9999 --detach --name bg blazegraph:2.1.5`

`# docker ps`

**Stop/Remove Docker container**

`# docker container rm bg --force`

**Or run in foreground**

`# docker container run --publish 9999:9999 --name bg blazegraph:2.1.5`

Kill by `Strg` + `C`

(Explanation of docker container commands see here: https://docs.docker.com/get-started/part2/#build-and-test-your-image)

**Done!**
Blazegraph can now be accessed via http://localhost:9999/blazegraph/#splash
