ARG BUILDER_BASEDIR="/app"

FROM ghcr.io/graalvm/native-image:ol7-java17-22.1.0 AS builder

# Variables
ARG TOOLCHAIN="x86_64-linux-musl-native"
ARG ZLIB_VERSION="zlib-1.2.12"
ARG BUILDER_BASEDIR

# Dependencies
RUN yum install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
RUN yum install -y upx tar xz gzip make

# Compile musl toolchain with zlib support
RUN curl http://more.musl.cc/10/x86_64-linux-musl/x86_64-linux-musl-native.tgz | tar -xzf -
RUN curl https://zlib.net/$ZLIB_VERSION.tar.xz | tar -xJf -
RUN export CC="$BUILDER_BASEDIR/$TOOLCHAIN/bin/gcc" && \
    cd $ZLIB_VERSION && \
    ./configure --prefix="$BUILDER_BASEDIR/$TOOLCHAIN" --static && \
    make && \
    make install

# Initial gradle setup (useless build for faster future compilation)
COPY gradle gradle
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
RUN ./gradlew build

# Actual native build
COPY . .
RUN export PATH="$BUILDER_BASEDIR/$TOOLCHAIN/bin:$PATH" && ./gradlew nativeBuild
RUN upx --lzma --best build/native/nativeBuild/MarkovBaj


# Alpine runtime setup
FROM alpine:3.15.4
ARG BUILDER_BASEDIR

WORKDIR /home
COPY --from=builder "$BUILDER_BASEDIR/build/native/nativeBuild/MarkovBaj" .
EXPOSE 8080
CMD ./MarkovBaj
