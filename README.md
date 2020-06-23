# Build local docker environment
make build ROOT_DIR=$(readlink -f .)

# Start local docker environment
make start

# Stop local docker environment
make stop

# Clean local docker environment
make clean