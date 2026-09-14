#!/bin/sh
set -e

echo "Pulling Ollama models for metis semantic search..."
ollama pull bge-m3
ollama pull llama3.2
echo "Ollama models ready."
