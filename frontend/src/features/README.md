# Features

Features represent user actions / user scenarios.

A feature includes everything needed for one action:
UI, state, validation, and API calls (for example login, rename list, accept request).

Typical contents inside a feature:
- `ui/` action UI (form, button group, modal content)
- `model/` feature state and logic
- `api/` requests for this action

Features can depend on:
- `entities`
- `shared`

Features should not depend on:
- `pages`
- `widgets`
- other features (prefer extracting shared logic to `entities` or `shared`)
