#!/bin/sh
# One-shot compose helper: after the Ollama service is healthy, ensure the Metis
# models exist (bge-m3 for embeddings, llama3.2 for index-time metadata).
#
# `ollama pull` downloads from Ollama's public registry (registry.ollama.ai).
# That needs outbound HTTPS. Clients that prohibit egress cannot rely on this
# script: pre-seed the Ollama data volume (./_db/ollama_data) from a machine
# that can pull, run `ollama create` from a local GGUF, or point Ollama at an
# internal mirror. If the model is already in the volume, pull is skipped.

set -e

ensure_model() {
  name="$1"
  if ollama list 2>/dev/null | awk 'NR > 1 { print $1 }' | grep -Eq "^${name}(:|$)"; then
    echo "Ollama model ${name} already present, skipping pull."
    return
  fi
  echo "Pulling Ollama model ${name} from the registry..."
  ollama pull "$name"
}

echo "Ensuring Ollama models for metis semantic search..."
ensure_model bge-m3
ensure_model llama3.2
echo "Ollama models ready."
