# Location Tracker implementation on spring reactive stack.
This project is written to practice spring reactive stack.

## Run book
You need Java 11 and Maven 3.6.3 or later

### Build
Build the project:
>mvn clean install

Quick build:
>mvn -T4 clean install -DskipTests -Dcheckstyle.skip

### Build local docker environment
make build ROOT_DIR=$(readlink -f .)

### Start local docker environment
make start

### Stop local docker environment
make stop

### Clean local docker environment
make clean