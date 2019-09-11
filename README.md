# Web of Needs Bot Skeleton ReadMe

This skeleton contains an echo bot that reacts to each new atom created on a given node. For each atom, the bot sends a configurable number of contact requests (default is 3) that can be accepted by the user. Within the established chat, the bot echoes all sent messages. 

> **NOTE:** Be careful with running more than one bot on a given node instance, as multiple bots may get into infinite loops.

The echo bot is a [Spring Boot Application](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-running-your-application.html).

## Prerequisites

- [Java 8 JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher installed 
- Java IDE of choice set up
- Maven frameworks set up

## Setting up

- Download or clone this repository
- Add config files
<!-- - Set up encryption with Bouncy Castle:

1. In `folder specified in config`, open a command line and run the following commands:
    1. `openssl req -x509 -newkey rsa:2048 -keyout t-key.pem -out t-cert.pem  -passout pass:changeit -days 365 -subj "//CN=myhost.mydomain.com"`
    1. `openssl pkcs12 -export -out tmpfile -passout pass:changeit -inkey t-key.pem -passin pass:changeit -in t-cert.pem`
    1. `"$JAVA_HOME/bin/keytool.exe" -importkeystore -srckeystore tmpfile -srcstoretype pkcs12 -destkeystore t-keystore.jks -deststoretype JKS -srcstorepass changeit  -deststorepass changeit`
    1. `rm tmpfile`
-->

## Running the echo bot

To start up the echo bot, use the following command line parameters, with paths adjusted to your environment:
```
-DWON_CONFIG_DIR=C:\DATA\DEV\bot-skeleton\conf
-DWON_NODE_URI=https://hackathonnode.matchat.org/won
-XX:PermSize=1024m
-XX:MaxPermSize=2048m
-Dlogback.configurationFile=C:\DATA\DEV\Source\bot-skeleton\conf\logback.xml
-Dlogging.config=C:\DATA\DEV\Source\bot-skeleton\conf\logback.xml
```

If you get a message indicating your keysize is restricted on startup, refer to [Step 3 of this tutorial](https://www.baeldung.com/java-bouncy-castle) to increase the allowed key size.

- for Java 8
    - download and install the [Java Cryptographic Extension](https://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
    - Install the Bouncy Castle security provider by navigating to the JRE installation directory and editing the file `lib/security/java.security`
    - Within `java.security`, find the list of providers and add the line `security.provider.11=org.bouncycastle.jce.provider.BouncyCastleProvider`
- for Java 9 and up
    - setting the crypto.policy property to unlimited by adding `Security.setProperty("crypto.policy", "unlimited");`

## Implement your own bot

Once the echo bot is running, you can use it as a base for implementing your own application. Refer to the general [Bot Readme](https://github.com/researchstudio-sat/webofneeds/blob/master/webofneeds/won-bot/README.md) for more information on Web of Needs Bot applications. 

### Hackathon Goals

* Run the bot successfully
* Change the bot to do something slightly different, e.g.
    * instead of an echo, let the bot send a fortune cookie message
    * only react to a specific type of atoms, like CyclingInterests
