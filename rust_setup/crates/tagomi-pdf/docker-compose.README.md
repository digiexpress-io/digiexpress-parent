# Docker Compose Setup

This docker-compose configuration runs the Tagomi PDF application with HTTPS enabled and custom fonts.

## Prerequisites

Before running docker-compose, you need to set up:

### 1. TLS Certificates

Create a `certs` directory and add your SSL certificates:

```bash
mkdir -p certs
```

**For development (self-signed certificates):**
```bash
openssl req -x509 -newkey rsa:4096 -keyout certs/key.pem -out certs/cert.pem -days 365 -nodes -subj "/CN=localhost"
```

**For production:**
Place your CA-signed certificates in the `certs` directory:
- `certs/cert.pem` - Your SSL certificate
- `certs/key.pem` - Your private key

### 2. Custom Fonts (Optional)

Create an `assets/fonts` directory and add your custom font files (.ttf, .otf):

```bash
mkdir -p assets/fonts
# Copy your font files to assets/fonts/
cp /path/to/your/fonts/*.ttf assets/fonts/
```

If you don't have custom fonts, the application will use system fonts.

## Usage

### Start the service:
```bash
docker compose up -d
```

### View logs:
```bash
docker compose logs -f
```

### Stop the service:
```bash
docker compose down
```

### Rebuild after code changes:
```bash
docker compose up -d --build
```

## Testing

Once running, test the service:

```bash
# Health check (with self-signed cert, use -k flag)
curl -k https://localhost:8085/health

# Should return: {"data":"Tagomi PDF - UP","success":true,"error":null}
```

## Configuration

Edit the environment variables in `docker-compose.yml` to customize:

- **HTTPS_ENABLED**: Set to `false` to disable HTTPS
- **PORT**: Change the application port
- **FONTS_PATH**: Path to custom fonts inside the container
- **USE_SYSTEM_FONTS**: Set to `false` to use only custom fonts
- **TLS_CERT_PATH**: Path to TLS certificate
- **TLS_KEY_PATH**: Path to TLS private key

## Directory Structure

```
tagomi-pdf/
├── docker-compose.yml
├── Dockerfile
├── Cargo.toml
├── src/
├── assets/
│   └── fonts/          # Custom fonts directory
│       ├── font1.ttf
│       └── font2.otf
└── certs/              # TLS certificates directory
    ├── cert.pem
    └── key.pem
```


