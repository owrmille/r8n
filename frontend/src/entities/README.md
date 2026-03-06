# Entities

Entities are domain objects and their reusable presentation.

Use this layer for core business nouns:
`opinion`, `opinion-list`, `access-request`, `selector`, `user`, etc.

Typical contents inside an entity:
- `model/` types, mappers, helpers, domain state
- `api/` read/query requests related to the entity
- `ui/` reusable entity presentation (card, badge, row, status label)

Entities can depend on:
- `shared`

Entities should not depend on:
- `features`
- `widgets`
- `pages`
