```bash
cargo run
```

```bash
 curl -X GET http://localhost:3000/health
```

```bash
 curl -X POST http://localhost:3000/compile \
  -H "Content-Type: application/json" \
  -d '{
    "main": "template-1",
    "now": "2025-09-26T15:30:45+05:00",
    "templates": [
      {
        "id": "template-1",
        "value": "#import user_data: inputs\n\n= Document Title\n\nHello #inputs.name, you are #inputs.age years old.\n\n#for item in inputs.items [\n  - #item\n]\n\nBest regards,\n#inputs.sender"
      }
    ],
    "props": [
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
  | base64 -d > font_test.pdf && open font_test.pdf
```
