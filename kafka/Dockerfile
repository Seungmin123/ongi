FROM apache/kafka:4.1.1

USER root
RUN mkdir -p /opt/kafka/jmx

COPY kafka/jmx/jmx_prometheus_javaagent-1.5.0.jar /opt/kafka/jmx/jmx_prometheus_javaagent-1.5.0.jar
COPY kafka/jmx/kafka.yml /opt/kafka/jmx/kafka.yml

# 권한 이슈 방지
RUN chown -R 1000:1000 /opt/kafka/jmx || true
USER 1000