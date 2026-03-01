# App

App is the application entry and global wiring layer.

Use this layer for:
- app bootstrap (`main.ts`)
- root component (`App.vue`)
- global providers (router, Pinia, query client, auth provider)
- global styles

App can depend on all lower layers, but try to keep it focused on initialization and composition.
Do not place domain business logic here.
