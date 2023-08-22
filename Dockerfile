# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.15
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/rse-idam-simulator.jar /opt/app/

EXPOSE 5556
CMD [ "rse-idam-simulator.jar" ]
