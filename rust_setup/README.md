```bash
 curl -X POST http://localhost:3000/compile \
  -H "Content-Type: application/json" \
  -d '{
    "content": "#import sys: inputs\n\n= Document Title\n\nHello #inputs.name, you are #inputs.age years old.\n\n#for item in inputs.items [\n  - #item\n]\n\nBest regards,\n#inputs.sender",
    "props": {
      "name": "Alice",
      "age": 30,
      "sender": "Bob",
      "items": ["Item 1", "Item 2", "Item 3"]
    }
  }' \
  | jq -r '.data.output' \
  | base64 -d > font_test.pdf && open font_test.pdf
```
