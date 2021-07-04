FROM ubuntu:20.04
MAINTAINER jwang
USER root

# Install OpenJDK-8
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y net-tools && \
    apt-get install -y vim && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    update-ca-certificates -f && \
    apt-get install -y telnetd && \
    apt-get install -y dnsutils && \
    apt-get install -y iputils-ping;

# install wget gpg
RUN apt-get install -y wget && \
    apt-get install -y gnupg && \
    apt-get install -y curl && \
    apt-get install -y jq && \
    apt-get install -y docker && \
    apt-get install -y libc-bin && \
    apt-get clean;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-arm64/
RUN export JAVA_HOME

# set up entry point
RUN mkdir /opt/livesink /opt/livesink/shared
WORKDIR /opt/livesink
ADD ./target/livesink-0.0.1-jar-with-dependencies.jar /opt/livesink/livesink.jar


RUN chmod +x /opt/livesink/livesink.jar
ADD entry_point.sh /opt/livesink/entry_point.sh
# entry point
ENTRYPOINT ["/opt/livesink/entry_point.sh"]