# Typst REST API

A REST API service built with Rust and Axum that provides Typst document compilation capabilities.

## Features

- Compile Typst documents to PDF or SVG
- Pre-built document templates
- RESTful API endpoints
- Error handling and logging
- CORS support

## API Endpoints

### Health Check
- `GET /` or `GET /health` - Check if the service is running

### Compile Document
- `POST /compile` - Compile Typst content to PDF or SVG

Request body:
```json
{
  "content": "= Hello World\n\nThis is a Typst document.",
  "format": "pdf"  // optional, defaults to "pdf", can be "svg"
}
```

Response:
```json
{
  "success": true,
  "data": {
    "output": "base64-encoded-content",
    "format": "pdf"
  },
  "error": null
}
```

### Get Available Templates
- `GET /templates` - Get list of available document templates

Response:
```json
{
  "success": true,
  "data": ["basic", "letter", "article", "report"],
  "error": null
}
```

### Get Template Content
- `GET /template?name=basic` - Get the content of a specific template

Response:
```json
{
  "success": true,
  "data": "template-content-here",
  "error": null
}
```

## Running the Service

1. Make sure you have Rust installed
2. Clone this project
3. Run with cargo:

```bash
cargo run
```

The service will start on `http://localhost:3000`

## Example Usage

### Using curl to compile a document:

```bash
curl -X POST http://localhost:3000/compile \
  -H "Content-Type: application/json" \
  -d '{
    "content": "= My Document\n\nHello from Typst!",
    "format": "pdf"
  }'
```

### Get a template:

```bash
curl "http://localhost:3000/template?name=basic"
```

### Decode the base64 output:

```bash
# Save the base64 output to a file and decode it
echo "base64-output-here" | base64 -d > output.pdf
```

## Dependencies

- **axum**: Web framework
- **tokio**: Async runtime
- **typst**: Typst compiler
- **typst-pdf**: PDF generation
- **serde**: Serialization
- **tower-http**: HTTP utilities including CORS

## Error Handling

The API returns structured error responses:

```json
{
  "success": false,
  "data": null,
  "error": "Error message here"
}
```

Common error scenarios:
- Invalid Typst syntax
- Unsupported output format
- Template not found

## Development

To extend this service:

1. Add new templates in `typst_service.rs`
2. Add new endpoints in `main.rs`
3. Implement additional Typst features as needed

The service uses a simplified World implementation for Typst compilation. For production use, you might want to implement a more sophisticated World that supports file imports, custom fonts, etc.