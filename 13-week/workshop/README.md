# Week 13 — Threat Modeling Workshop

## CP0 — Workshop setup

<!-- CONFIG-START -->
- FULL_NAME: Monica Alexandra Diaz Tovar
- GITHUB_USER: 7714095Alexandra
- TEAM: by_sellens
- DOCS_REPO: https://github.com/code-corhuila/bysellens-docs
- BOUNDARY: React admin UI → inventory-service product registration, POST /api/v1/inventory/products
- CLAIM_ISSUE: https://github.com/code-corhuila/bysellens-docs/issues/54
- REFLECTION_DOC: https://docs.google.com/document/d/1YDt45oYGj1d0XUBGX1jPpDSTwUM3DbhM56toLEdlWoI/edit?usp=sharing
- STORIES: https://github.com/code-corhuila/bysellens-docs/issues/61, https://github.com/code-corhuila/bysellens-docs/issues/67, https://github.com/code-corhuila/bysellens-docs/issues/62
- LIBRARY_BOOK: Costas Santos, Jesús. Seguridad informática. First edition. Bogotá: Ediciones de la U, 2011. ISBN 978-958-8675-70-1. Consulted pages: 90–93. Call number: Pending confirmation with the library.
- LIBRARY_RECORD_TIME: Pending confirmation with the library.
<!-- CONFIG-END -->

## Scope

- Analyze the trust boundary between the administrator's React browser client and inventory-service when registering products through POST /api/v1/inventory/products.
- Include product data, purchase and sale prices, and initialStock.
- Exclude downstream ProductCreated processing and current-stock management, which belongs to product-service.
- Record the documentation discrepancy: the second-cut architecture requires JWT validation and ADMIN authorization, but the OpenAPI declares security: []. Actual enforcement remains to be verified; this is not a confirmed vulnerability.

## Pending information

- Library call number and visit record date and time in America/Bogota remain pending confirmation with the library.

## CP7 review

- Issues #61, #67 and #62 were reviewed against STRIDE: E-01, S-01 and T-01 respectively. Each has verifiable acceptance criteria; issue #61 also contains the [B] book-derived criterion. Project inclusion remains not independently verified.
