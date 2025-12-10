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

# Runtime stage
FROM debian:bookworm-slim

# Install runtime dependencies
RUN apt-get update && \
    apt-get install -y ca-certificates libssl3 && \
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

