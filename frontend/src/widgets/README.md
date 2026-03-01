# Widgets

Widgets are large UI blocks used inside pages.

A widget is not a single user action. It is a visual section that can combine
multiple features and entities (for example a header, table panel, recommendations panel).

Typical contents:
- `ui/` visual components
- `model/` widget-local state (sorting, expanded rows, local UI behavior)

Widgets can depend on:
- `features`
- `entities`
- `shared`

Widgets should not depend on `pages`.
