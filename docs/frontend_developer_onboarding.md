# Frontend Developer Onboarding

## Backend Setup (Reference Docs)

- most environment variables here are used only for the guide process convenience, they are not needed later when IDEA is set up. Exceptions will be marked explicitly. Theoretically you can skip most of those unexceptional envvars if you're ready to type the full texts instead.
- `export BACKEND=~/PROJECTS/r8n` adjust to your setup

### Java Development Kit
Execution engine for backend. Used every time to build and run the project.
- `java --version`, `javac --version` in terminal should show same version 21. If you have a more modern version, it may work depending on whether you manage to force it to use language standard 21 while compiling. So if you see a more modern version, you can try skipping this section and going right to running section. If any problems along the lines of "JDK toolchain 21 not found" arise, you'd have to set up existing JDK or install an older one.
- download an archive for JDK 21
- campus:
    - `mkdir -p /sgoinfre/goinfre/Perso/$USER/jdk && tar -xzf ~/Downloads/OpenJDK21U-jdk_x64_linux_hotspot_21.0.10_7.tar.gz -C /sgoinfre/goinfre/Perso/$USER/jdk` (using this in examples below)
    - `export JAVA_HOME=/sgoinfre/goinfre/Perso/$USER/jdk/jdk21.0.10+7`
- personal machine if you're ready for a system-wide installation:
    - `mkdir -p /opt/jdk && tar -xzf ~/Downloads/OpenJDK21U-jdk_x64_linux_hotspot_21.0.10_7.tar.gz -C /opt/jdk`
    - `export JAVA_HOME=/opt/jdk/jdk21.0.10+7`
- personal machine with a single-user installation:
    - `mkdir -p ~/jdk && tar -xzf ~/Downloads/OpenJDK21U-jdk_x64_linux_hotspot_21.0.10_7.tar.gz -C ~/jdk`
    - `export JAVA_HOME=~/jdk/jdk21.0.10+7`
- (below: select the configuration file for your preferred terminal that you plan to use for building the backend with Gradle; pay attention to different quotes)
- `echo "export JAVA_HOME=$JAVA_HOME" >> ~/.zshrc` (.bashrc etc.) (this export is not global, it works only for your selected terminal, but this envvar is unskippable, it tells the terminal where to look for java)
- `echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc` (same as above)
- (reloading Gradle and terminal to make sure all changes are picked up)
- `cd $BACKEND && ./gradlew --stop`
- `pkill -f GradleDaemon`
- close all terminal instances and open a new one
- check `java --version` and `javac --version` again
- `export BACKEND=~/PROJECTS/r8n`
- `cd $BACKEND && ./gradlew javaToolchains`: should display your JDK and 'is JDK: true'

### Gradle
Build and dependency management system for backend. Runs every time you build backend, also probably you'd call Gradle wrapper (gradlew) to run the backend too. Included into the repository, but its cache (downloaded libraries) is stored separately. If you're setting up a personal machine, skip to the next section. If you're setting up a campus machine, you'll have to move Gradle cache to sgoinfre, it's too big for your allowed home folder volume.
- `mkdir -p /sgoinfre/goinfre/Perso/$USER/gradle`
- `rm -rf ~/.gradle`
- `ln -s /sgoinfre/goinfre/Perso/$USER/gradle ~/.gradle` (now ~/.gradle is not a Gradle cache folder, rather a link to an actual cache folder in sgoinfre)
- `echo "export GRADLE_USER_HOME=/sgoinfre/goinfre/Perso/$USER/gradle" >> ~/.zshrc` (or your terminal config) (now ~/.gradle is not a Gradle cache folder, rather a link to an actual cache folder in sgoinfre)
- (reloading Gradle and terminal to make sure all changes are picked up)
- `cd $BACKEND && ./gradlew --stop`
- `pkill -f GradleDaemon`
- close all terminal instances and open a new one

### Running backend
- `make docker-run-database`
- `make local-run-all` and check all the log files for Started application in X seconds
- `make direct-request-opinion` and `make routed-request-opinion` should provide same result, with 0000..0 id

Useful context:

- [End user workflow](end_user_workflow.md)

## Frontend setup
- open a new terminal
- `export FRONTEND=~/PROJECTS/r8n/frontend` adjust to your setup
- check if Node.js is installed:
  - `node -v`
  - if command is not found, install Node via nvm:
    - `curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash`
    - restart terminal
- use the project's Node version:
  - `cd $FRONTEND`
  - `nvm install`
  - `nvm use`
- verify versions:
  - `node -v` should be `>= 22.13.0`
  - `npm -v` should be `10+`
- if you run frontend on a campus machine, move npm cache to sgoinfre:
  - `npm config set cache /sgoinfre/goinfre/Perso/$USER/.npm-cache --global`
  - `npm config get cache` should print `/sgoinfre/goinfre/Perso/$USER/.npm-cache`
- install dependencies:
  - `npm ci`
- open `frontend.code-workspace` in VSCode
- install recommended extensions for this workspace:
  - `ESLint`
  - `Prettier - Code formatter`
  - `Tailwind CSS IntelliSense`
- start frontend dev server:
  - `npm run dev` for standalone UI work
  - `make frontend-dev` if you need frontend proxying to local backend over HTTPS with project certificate
  - as soon as you see `Local: http://127.0.0.1:5173/` in logs, the app is running
- open `http://127.0.0.1:5173` in browser
- for API integration keep backend services running in another terminal: `make local-run-all`

## Frontend commands for daily work
- `cd ~/PROJECTS/r8n/frontend`
- `nvm use`
- `npm run dev` to run UI locally
- `make frontend-dev` to run UI with gateway certificate when proxying `/api` to local backend
- `npm run lint` to run ESLint
- `npm test` to run Vitest tests
- `npm run test:e2e` to run Playwright tests (currently there are no checked-in e2e specs, so this command reports `No tests found`)
- `npm run build` to verify production build
- `npm run build:dev` to verify development-mode build
- `npm run preview` to preview the production build locally

## Frontend after repository update
- stop frontend dev server in running terminal (`Ctrl-C`)
- update repository:
  - `cd ~/PROJECTS/r8n`
  - `git pull`
- sync frontend environment:
  - `cd ~/PROJECTS/r8n/frontend`
  - `nvm use`
  - `npm ci`
- validate local state:
  - `npm run lint`
  - `npm test`
  - `npm run build`
- start frontend again:
  - `npm run dev` for standalone UI work
  - `make frontend-dev` if you need backend API proxying over HTTPS
- if backend files were updated too, restart backend services: `make local-stop-all && make local-run-all`

## Stack & architecture (brief)
- frontend stack:
  - [`React 18`](https://react.dev/) + [`TypeScript`](https://www.typescriptlang.org/docs/)
  - [`Vite`](https://vite.dev/guide/) (dev server and build)
  - [`React Router`](https://reactrouter.com/) for routing
  - [`@tanstack/react-query`](https://tanstack.com/query/latest) for query state
  - [`Tailwind CSS v3`](https://tailwindcss.com/docs/installation/using-vite)
  - local `shadcn/ui`-style components built on Radix UI primitives
  - [`Vitest`](https://vitest.dev/) for unit tests
  - [`Playwright`](https://playwright.dev/) for e2e testing infrastructure
- current top-level structure in `src`:
  - `App.tsx` app bootstrap, router, providers, and layout wiring
  - `pages` route-level screens
  - `components/layout` app shell and navigation
  - `components/ui` shared UI primitives
  - `components` page-level reusable components
  - `hooks`, `lib`, `assets`, `test` support code and utilities
