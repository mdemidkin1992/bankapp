#!/bin/bash

# Upload Docker Images to Minikube Script
# This script loads all built Docker images into minikube

set -e

echo "Uploading Docker images to minikube..."

# List of services to upload
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

# Load each service image into minikube
for service in "${services[@]}"; do
    echo "Loading $service image into minikube..."
    minikube image load "${service}-app:latest"
    echo "✓ Loaded ${service}-app:latest into minikube"
done

echo "All Docker images loaded into minikube successfully!"

# Verify images are loaded
echo "Verifying images in minikube:"
minikube image ls | grep -E "(service-.*-app|REPOSITORY)"