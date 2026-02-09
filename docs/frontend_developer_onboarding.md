- most environment variables here are used only for the guide process convenience, they are not needed later when your machine is set up. Exceptions will be marked explicitly. Theoretically you can skip most of those unexceptional envvars if you're ready to type the full texts instead.
- `export BACKEND=~/PROJECTS/r8n` adjust to your setup

# Java Development Kit
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

# Gradle
Build and dependency management system for backend. Runs every time you build backend, also probably you'd call Gradle wrapper (gradlew) to run the backend too. Included into the repository, but its cache (downloaded libraries) is stored separately. If you're setting up a personal machine, skip to the next section. If you're setting up a campus machine, you'll have to move Gradle cache to sgoinfre, it's too big for your allowed home folder volume.
- `mkdir -p /sgoinfre/goinfre/Perso/$USER/gradle`
- `rm -rf ~/.gradle`
- `ln -s /sgoinfre/goinfre/Perso/$USER/gradle ~/.gradle` (now ~/.gradle is not a Gradle cache folder, rather a link to an actual cache folder in sgoinfre)
- `echo "export GRADLE_USER_HOME=/sgoinfre/goinfre/Perso/$USER/gradle" >> ~/.zshrc` (or your terminal config) (now ~/.gradle is not a Gradle cache folder, rather a link to an actual cache folder in sgoinfre)
- (reloading Gradle and terminal to make sure all changes are picked up)
- `cd $BACKEND && ./gradlew --stop`
- `pkill -f GradleDaemon`
- close all terminal instances and open a new one

# Running backend
- `export BACKEND=~/PROJECTS/r8n`
- `cd $BACKEND && ./gradlew :gateway:bootRun`
- as soon as you see 'Started GatewayApplicationKt in X seconds' in the logs in the terminal - the app is running
- if you see something about wrong Java version, return to JDK installation/setup
- if you see something about Gradle not having enough space, return to moving Gradle cache to another partition
- another terminal or browser: `curl "localhost:8080?id=723b8c60-bbbb-4814-90b8-2e6a1594102e"` (random valid UUID), get a stub response
- return to running terminal, Ctrl-C to terminate
