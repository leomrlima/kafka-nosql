FROM azul/zulu-openjdk:8

ADD target/lib /usr/share/consumer/lib
ADD target/consumer.jar /usr/share/consumer

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/consumer/consumer.jar", "-XX:+UseCGroupMemoryLimitForHeap"]