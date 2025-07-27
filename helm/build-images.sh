#!/bin/bash

# Build Docker Images Script
# This script builds all microservice Docker images

set -e

echo "Building Docker images for bankapp microservices..."

# Change to project root directory
cd /Users/maximdemidkin/Desktop/practicum/bankapp

# List of services to build
services=(
    "service-accounts"
    "service-blocker" 
    "service-cash"
    "service-convert"
    "service-exchange"
    "service-front"
    "service-gateway"
    "service-notifications"
    "service-transfer"
)

# Build each service image
for service in "${services[@]}"; do
    echo "Building $service image..."
    docker build -t "${service}-app:latest" -f "$service/Dockerfile" .
    echo "✓ Built ${service}-app:latest"
done

echo "All Docker images built successfully!"
echo "Built images:"
for service in "${services[@]}"; do
    echo "  - ${service}-app:latest"
done