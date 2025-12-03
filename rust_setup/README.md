### Installation and Running

- Ensure you have Rust installed. If not, you can install it from [rustup.rs](https://rustup.rs/).
- Rust uses `cargo` as its package manager and build tool, which comes bundled with Rust.

To run the PDF application, navigate to the project directory and execute:

```bash
cargo run
```

### Calling endpoints

```bash
 curl -X GET http://localhost:8085/health
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
