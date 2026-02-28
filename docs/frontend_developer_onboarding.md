# Frontend Developer Onboarding

## Backend Setup (Reference Docs)

Before working with frontend API integration, set up backend using one of these guides:

- [Backend developer onboarding (VS Code)](backend_developer_onboarding_VSCode.md)
- [Backend developer onboarding (IntelliJ IDEA)](backend_developer_onboarding_IntelliJIDEA.md)

Useful context:

- [End user workflow](end_user_workflow.md)

## Prerequisites

Install these tools before running frontend locally:

- `Node.js` `^20.19.0 || >=22.12.0`
- `npm` (comes with Node.js, recommended `npm 10+`)

Install command examples:

### macOS

Recommended (`nvm`):

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash

# restart terminal, then:
nvm install 22
nvm use 22
nvm alias default 22
```

Alternative (Homebrew):

```bash
brew install node@22
```

### Ubuntu

Recommended (`nvm`):

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash

# restart terminal, then:
nvm install 22
nvm use 22
nvm alias default 22
```

Alternative (APT, Ubuntu 22.04+):

```bash
sudo apt update
sudo apt install -y nodejs npm
```

If your `npm` is older than required, update it:

```bash
npm install -g npm@latest
```

Check your versions:

```bash
git --version
node --version
npm --version
```

For frontend API integration, backend services should also be running locally (gateway + services).

## IDE Setup

For frontend development in this project, use **VS Code**.

Open the existing workspace file from the repository root:

- `frontend.code-workspace`

How to open extension recommendations in VS Code:

1. Open the Extensions view (`Cmd+Shift+X` on macOS, `Ctrl+Shift+X` on Linux).
2. In Command Palette (`Cmd/Ctrl+Shift+P`), run `Extensions: Show Recommended Extensions`.
3. Install all workspace recommendations.

Install at least these extensions:

- `Vue - Official` (Volar)
- `ESLint`
- `Prettier - Code formatter`
- `Tailwind CSS IntelliSense`

If `Vetur` is installed, disable it for this workspace (Volar should be used instead).

## Stack & Architecture (Brief)

### Frontend stack

- `Vue 3` + `TypeScript`
- `Vite` (dev server and build)
- `Vue Router`
- `Pinia`
- `Tailwind CSS v4`
- `shadcn-vue` (UI primitives)

### Architecture (FSD-lite)

Project structure follows `FSD-lite`:

- `app` — app bootstrap, global providers, router
- `pages` — route-level screens
- `widgets` — large reusable UI blocks
- `features` — user actions (login, create opinion, etc.)
- `entities` — domain models and entity API
- `shared` — common UI, utilities, and API client

Rules to keep in mind:

- Keep network layer in `shared/api` and domain API in `entities/*/api` or `features/*/api`.
- Page files should mostly compose existing widgets/features instead of containing heavy business logic.

## Project Bootstrap

Run all commands in this section from the frontend directory:

```bash
cd ~/r8n/frontend
```

### Install frontend dependencies

```bash
npm install
```

If you want a clean install exactly from lockfile:

```bash
npm ci
```

### Start frontend

```bash
npm run dev
```

Open the URL shown in terminal (usually `http://localhost:5173`).
