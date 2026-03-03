# Docker Architecture (HTTPS at the Edge)

This document explains the Docker networking layout, TLS termination, and why only the public edge uses HTTPS.

## Core Idea

- HTTPS is required at the **edge** (browser <-> Nginx) to protect user traffic.
- Inside the Docker network, services can use **HTTP** because traffic stays on a private, isolated bridge.
- Nginx terminates TLS and forwards requests to the gateway over HTTP.

## High-Level Flows

Local dev (no Docker):

```mermaid
graph LR
  Browser["Browser"]
  Vite["Vite Dev Server (localhost:5173)"]
  Gateway["Gateway API (localhost:8080)"]
  Opinions["Opinions Service (localhost:8081)"]
  Mock["Mock Service (localhost:8090)"]

  Browser -->|"http://localhost:5173"| Vite
  Vite -->|"/api -> http://localhost:8080"| Gateway
  Gateway -->|"http://localhost:8081"| Opinions
  Gateway -->|"http://localhost:8090"| Mock
```

Local production (Docker):

```mermaid
graph LR
  Browser["Browser"]
  Nginx["Nginx (frontend)"]
  Gateway["Gateway API (gateway:8080)"]
  Opinions["Opinions Service (opinions:8080)"]
  Mock["Mock Service (mock:8080)"]

  Browser -->|"HTTPS :443"| Nginx
  Nginx -->|"HTTP :8080"| Gateway
  Gateway -->|"HTTP :8080"| Opinions
  Gateway -->|"HTTP :8080"| Mock
```

## Runtime API Sequence (Nginx + Gateway)

```mermaid
sequenceDiagram
  participant B as Browser
  participant N as Nginx
  participant G as Gateway

  B->>N: GET https://localhost/
  N-->>B: HTML/JS/CSS (Vue app)

  B->>N: API call /api/...
  N->>G: Proxy to http://gateway:8080/...
  G-->>N: JSON response
  N-->>B: JSON response
```

## Dev vs Runtime

Dev (Vite):
- Browser talks directly to the Vite dev server at `http://localhost:5173`.
- Vite proxies `/api` to the gateway at `http://localhost:8080`.
- The gateway reaches services on `localhost:8081` and `localhost:8090`.
- No Nginx involved.

Runtime (local production):
- Browser talks to Nginx over `https://localhost`.
- Nginx serves static assets and proxies `/api` to the gateway at `http://gateway:8080`.
- The gateway reaches services by name on `opinions:8080` and `mock:8080`.
- This mirrors a production edge setup with TLS termination.

## Ports by Mode

Local dev (no Docker):
- `gateway` -> `localhost:8080`
- `opinions` -> `localhost:8081`
- `mock` -> `localhost:8090`

Local production (Docker):
- `gateway` -> `gateway:8080`
- `opinions` -> `opinions:8080`
- `mock` -> `mock:8080`

Inside Docker, containers are isolated. Multiple services can use the same port because each service has its own hostname.
From the host machine, only published ports are reachable. In this setup:
- `localhost:8080` maps to `gateway:8080`

## Docker Network View

```mermaid
graph TB
  subgraph Host["Host Machine"]
    Browser["Browser"]
  end

  subgraph Docker["Docker Bridge Network (r8n_net)"]
    Nginx["frontend (nginx:443)"]
    Gateway["gateway (8080)"]
    Opinions["opinions (8080)"]
    Mock["mock (8080)"]
  end

  Browser -->|"HTTPS :443"| Nginx
  Nginx -->|"HTTP :8080"| Gateway
  Gateway -->|"HTTP :8080"| Opinions
  Gateway -->|"HTTP :8080"| Mock
```

## Why HTTPS Only at the Edge

- The browser is outside the Docker network, so traffic must be encrypted.
- The Docker bridge network is isolated and private to the host machine.
- Encrypting internal service-to-service traffic adds complexity and is not required for local-only deployments.
- This mirrors real production setups that use a load balancer or Nginx to terminate TLS.

## Notes

- If deployed to a public environment, internal TLS can be added as well.
- For local production, TLS at the edge is enough and keeps the setup simple.
