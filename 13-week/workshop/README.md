# Week 13 — Threat Modeling Workshop

## CP0 — Workshop setup

<!-- CONFIG-START -->
- FULL_NAME: Monica Alexandra Diaz Tovar
- GITHUB_USER: 7714095Alexandra
- TEAM: by_sellens
- DOCS_REPO: https://github.com/code-corhuila/bysellens-docs
- BOUNDARY: React admin UI → inventory-service product registration, POST /api/v1/inventory/products
- CLAIM_ISSUE: [PEGA AQUÍ LA URL DEL ISSUE]
- REFLECTION_DOC: [PEGA AQUÍ LA URL DEL NUEVO GOOGLE DOCS]
- LIBRARY_BOOK: Costas Santos, Jesús. Seguridad informática. First edition. Bogotá: Ediciones de la U, 2011. ISBN 978-958-8675-70-1. Consulted pages: 90–93. Library call number: pending confirmation.
- LIBRARY_RECORD_TIME: pending confirmation
<!-- CONFIG-END -->

## Scope

The selected trust boundary is between the administrator's React browser client and inventory-service, limited to product registration through POST /api/v1/inventory/products. Inventory receives product information and initialStock. Downstream ProductCreated processing and current-stock management in product-service are excluded.

## Pending information

- Confirmed URL of the CP0 reservation issue.
- URL of the new Google Docs document for the personal reflection.
- Library call number and library record time.
