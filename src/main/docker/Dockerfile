FROM idp-server-base:latest as build

RUN mkdir build

COPY . /build

RUN cd /build && \
    tar -xzf ${basename}-bin.tar.gz && \
    rm ${basename}-bin.tar.gz && \
    find . -type d -exec chmod 755 {} \;

FROM idp-server-base:latest as image

ENV HOME=/idp-server

COPY --from=build /build ${HOME}

WORKDIR ${HOME}

RUN mkdir ${HOME}/ks

RUN mv idp-0.0.1-SNAPSHOT.jar lib/idp-0.0.1-SNAPSHOT.jar

RUN addgroup -S idp && adduser -S idp -G idp && chown -R idp:idp ${HOME}

USER idp:idp

CMD [ "java", "-cp", "lib/*:.", "com.forristall.general.idp.IdpApplication" ]