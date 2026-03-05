# Pages

Pages are route-level screens.

Use this layer to compose the full screen from widgets, features, entities, and shared UI.
Keep business logic here minimal. A page should mostly arrange layout and connect modules.

Typical contents:
- `ui/` route component(s) (for example `AuthLoginPage.vue`)
- `model/` page-local state only (tabs, query params, page-only filters)

Pages can depend on:
- `widgets`
- `features`
- `entities`
- `shared`

Pages should not be used by other layers.
