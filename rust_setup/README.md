### Installation and Running

- Ensure you have Rust installed. If not, you can install it from [rustup.rs](https://rustup.rs/).
- Rust uses `cargo` as its package manager and build tool, which comes bundled with Rust.

To run the PDF application, navigate to the project directory and execute:

```bash
cargo run
```

### Configuration

The application is configured using environment variables:

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `HTTPS_ENABLED` | Enable HTTPS mode (`true` or `false`) | `false` |
| `PORT` | Port to listen on | `8085` |
| `TLS_CERT_PATH` | Path to TLS certificate file | `/etc/tls/cert.pem` |
| `TLS_KEY_PATH` | Path to TLS private key file | `/etc/tls/key.pem` |
| `FONTS_PATH` | Path to custom fonts directory | `/assets/fonts` |
| `USE_SYSTEM_FONTS` | Include system fonts (`true` or `false`) | `true` |

#### Running in HTTP mode (default):
```bash
cargo run
```

#### Running in HTTPS mode:
```bash
# Generate test certificates (for development only)
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes -subj "/CN=localhost"

# Run with HTTPS
HTTPS_ENABLED=true TLS_CERT_PATH=cert.pem TLS_KEY_PATH=key.pem cargo run
```

#### Running on a custom port:
```bash
PORT=9090 cargo run
```

#### Using custom fonts:
```bash
# Use custom fonts in addition to system fonts (default behavior)
FONTS_PATH=/path/to/fonts cargo run

# Use custom fonts only (disable system fonts)
FONTS_PATH=/path/to/fonts USE_SYSTEM_FONTS=false cargo run

# Use system fonts only (default if FONTS_PATH not set)
cargo run
```

### Running with Docker

```bash
docker-compose up -d
```

See [docker-compose.README.md](crates/tagomi-pdf/docker-compose.README.md) for more details.


### Calling endpoints

```bash
# HTTP mode
curl -X GET http://localhost:8085/health

# HTTPS mode (with self-signed certificate)
curl -k -X GET https://localhost:8085/health
```

```bash
 curl -X POST http://localhost:8085/compile \
  -H "Content-Type: application/json" \
  -d '{
    "main_template_id": "template-1",
    "timestamp": "2025-09-26T15:30:45+05:00",
    "templates": [
      {
        "id": "template-1",
        "value": "#let inputs = sys.inputs.user_data.inputs\n\n= Document Title\n\nHello #inputs.name, you are #inputs.age years old.\n\n#for item in inputs.items [\n  - #item\n]\n\nBest regards,\n#inputs.sender"
      }
    ],
    "data_modules": [
      {
        "module_name": "user_data",
        "body_name": "inputs",
        "body_value": {
          "name": "Alice",
          "age": 30,
          "sender": "Bob",
          "items": ["Item 1", "Item 2", "Item 3"]
        }
      }
    ]
  }' \
  | jq -r '.data.base64' \
  | base64 -d > font_test.pdf
```

```bash
curl -s -X POST http://localhost:8085/compile \
  -H "Content-Type: application/json" \
  -d '{
    "main_template_id": "greeting",
    "timestamp": "2025-12-01T20:00:00+00:00",
    "templates": [
      {
        "id": "greeting",
        "value": "#set page(width: 200pt, height: 150pt, margin: 20pt)\n#set text(size: 14pt)\n\n= Hello from Typst!\n\nWelcome *#sys.inputs.data.user.name*!\n\nYou have #sys.inputs.data.user.points points.\n\n#if sys.inputs.data.user.premium [\n  _Thank you for being a premium member!_\n] else [\n  _Upgrade to premium for more features._\n]"
      }
    ],
    "data_modules": [
      {
        "module_name": "data",
        "body_name": "user",
        "body_value": {
          "name": "Alice",
          "points": 1250,
          "premium": true
        }
      }
    ]
  }' | jq -r '.data.base64' | base64 -d > example_output.pdf
```


### Using Images in PDFs

```bash
# Change image.jpg to the path of the image you want to show
IMAGE_BASE64=$(base64 -i image.jpg)
curl -s -X POST http://localhost:8085/compile \
  -H 'Content-Type: application/json' \
  -d '{
    "main_template_id": "greeting_with_image",
    "timestamp": "2025-12-01T20:00:00+00:00",
    "templates": [
      {
        "id": "greeting_with_image",
        "value": "#set page(width: 400pt, height: 600pt, margin: 20pt)\n#set text(size: 14pt)\n\n= Hello from Typst with Image!\n\nWelcome *#sys.inputs.data.user.name*!\n\nYou have #sys.inputs.data.user.points points.\n\n#if sys.inputs.data.user.premium [\n  _Thank you for being a premium member!_\n] else [\n  _Upgrade to premium for more features._\n]\n\n== Your Profile Picture\n\n#image.decode(sys.inputs.images.profile_picture, width: 200pt)\n\n_Image embedded successfully!_"
      }
    ],
    "data_modules": [
      {
        "module_name": "data",
        "body_name": "user",
        "body_value": {
          "name": "Alice",
          "points": 1250,
          "premium": true
        }
      },
      {
        "module_name": "images",
        "body_name": "profile_picture",
        "body_value": {
          "__base64__": "'$IMAGE_BASE64'"
        }
      }
    ]
  }' | jq -r '.data.base64' | base64 -d > example_with_image.pdf

```
