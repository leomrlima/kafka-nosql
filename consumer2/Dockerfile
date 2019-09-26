FROM azul/zulu-openjdk:8

RUN mkdir -p /usr/share/consumer2

ADD target/consumer-thorntail.jar /usr/share/consumer2

ENTRYPOINT ["/usr/bin/java", "-Djava.net.preferIPv4Stack=true", "-jar", "/usr/share/consumer2/consumer-thorntail.jar", "-XX:+UseCGroupMemoryLimitForHeap"]