FROM azul/zulu-openjdk:8

LABEL maintainer="Leonardo Lima <leomrlima@gmail.com>"

ADD target/lib /usr/share/producer/lib
ADD target/producer.jar /usr/share/producer

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/producer/producer.jar", "-XX:+UseCGroupMemoryLimitForHeap"]