FROM docker-private.infra.cloudera.com/cloudera_base/ubi8/cldr-openjdk-17-runtime-cis:1.15-1.1682053056-cis-08062023
MAINTAINER info@cloudera.com

# REPO URL to download jar
ARG REPO_URL=https://nexus-private.hortonworks.com/nexus/content/groups/public
ARG VERSION=''

ENV VERSION ${VERSION}

WORKDIR /

# install the cloudbreak app
ADD ${REPO_URL}/com/sequenceiq/mock-infrastructure/$VERSION/mock-infrastructure-$VERSION.jar /mock-infrastructure.jar

CMD (java --add-opens java.base/java.util=ALL-UNNAMED -jar /mock-infrastructure.jar) & JAVAPID=$!; trap "kill $JAVAPID; wait $JAVAPID" INT TERM; wait $JAVAPID
