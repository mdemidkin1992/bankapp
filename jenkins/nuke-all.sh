#!/bin/bash
set -e

# Загрузка переменных из .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Проверка переменной
if [ -z "$DOCKER_REGISTRY" ]; then
  echo "DOCKER_REGISTRY не задан в .env"
  exit 1
fi

echo "Using DOCKER_REGISTRY: $DOCKER_REGISTRY"

echo "Uninstalling Helm releases..."
for ns in bankapp-dev bankapp-test bankapp-prod; do
  helm uninstall bankapp -n "$ns" || true
done

echo "Force deleting remaining resources..."
for ns in bankapp-dev bankapp-test bankapp-prod; do
  # Принудительное удаление pods, statefulsets, deployments
  kubectl delete pods --all -n "$ns" --grace-period=0 --force || true
  kubectl delete statefulsets --all -n "$ns" --grace-period=0 --force || true
  kubectl delete deployments --all -n "$ns" --grace-period=0 --force || true
  
  # Удаление PVCs
  kubectl delete pvc --all -n "$ns" --ignore-not-found || true
  
  # Удаление services и configmaps
  kubectl delete svc --all -n "$ns" --ignore-not-found || true
  kubectl delete configmaps --all -n "$ns" --ignore-not-found || true
  kubectl delete secrets --all -n "$ns" --ignore-not-found || true
  kubectl delete servicemonitors --all -n "$ns" --ignore-not-found || true
done

echo "Deleting PVs..."
kubectl delete pv --all || true

echo "Deleting namespaces..."
kubectl delete ns bankapp-dev --ignore-not-found
kubectl delete ns bankapp-test --ignore-not-found
kubectl delete ns bankapp-prod --ignore-not-found

echo "Shutting down Jenkins..."
docker compose down -v || true
docker stop jenkins && docker rm jenkins || true
docker volume rm jenkins_home || true

echo "Removing images..."
docker image rm ${DOCKER_REGISTRY}/service-accounts || true
docker image rm ${DOCKER_REGISTRY}/service-apigw || true
docker image rm ${DOCKER_REGISTRY}/service-cash || true
docker image rm ${DOCKER_REGISTRY}/service-transfer || true
docker image rm ${DOCKER_REGISTRY}/service-convert || true
docker image rm ${DOCKER_REGISTRY}/service-exchange || true
docker image rm ${DOCKER_REGISTRY}/service-blocker || true
docker image rm ${DOCKER_REGISTRY}/service-notifications || true
docker image rm ${DOCKER_REGISTRY}/service-front || true
docker image rm jenkins/jenkins:lts-jdk21 || true

echo "Pruning system..."
docker system prune -af --volumes

echo "Done! All clean."
