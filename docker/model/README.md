# Model container (local NVIDIA model)

This folder contains guidance for running the local model container used in the demo.

Readiness probe
- The model container SHOULD expose an HTTP health endpoint (e.g., /health) returning 200 when ready.
- Quickstart and docker-compose.yaml reference the container image via the MODEL_IMAGE env var.

License and provenance
- Document the model image name and license in README.md when selecting an image.

Security
- Avoid embedding keys in images; pass API keys via environment variables or docker secrets.
