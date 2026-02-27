# Shared

Shared is the foundation layer.

Put here code that is generic and not tied to a business domain:
UI kit, API client, utils, constants, routing helpers, config, common types.

Examples:
- `ui/` buttons, inputs, modal, table primitives
- `api/` base HTTP client
- `lib/` pure utilities
- `types/` common types
- `styles/` global styles

Shared should not import from domain layers (`entities`, `features`, `widgets`, `pages`).
