# Build stage
FROM rust:1.91 AS builder

WORKDIR /app

# Copy manifests
COPY Cargo.toml Cargo.lock ./

# Copy source code
COPY src ./src
COPY assets ./assets

# Build the application in release mode
RUN cargo build --release

# Pre-download Typst packages
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && \
    mkdir -p assets/packages/preview/cetz/0.4.2 && \
    curl -fL https://packages.typst.org/preview/cetz-0.4.2.tar.gz \
        | tar -xz -C assets/packages/preview/cetz/0.4.2 && \
    mkdir -p assets/packages/preview/cetz-plot/0.1.3 && \
    curl -fL https://packages.typst.org/preview/cetz-plot-0.1.3.tar.gz \
        | tar -xz -C assets/packages/preview/cetz-plot/0.1.3 && \
    mkdir -p assets/packages/preview/oxifmt/1.0.0 && \
    curl -fL https://packages.typst.org/preview/oxifmt-1.0.0.tar.gz \
        | tar -xz -C assets/packages/preview/oxifmt/1.0.0

# Runtime stage
FROM debian:bookworm-slim

# Install runtime dependencies
RUN apt-get update && \
    apt-get install -y ca-certificates libssl3 fontconfig fonts-roboto && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the binary from builder
COPY --from=builder /app/target/release/tagomi-pdf /usr/local/bin/tagomi-pdf

# Copy assets
COPY --from=builder /app/assets ./assets

# Expose default port
EXPOSE 8085

# Run the application
CMD ["tagomi-pdf"]

