#!/bin/bash

set -e

ERR_MSG=''

# 로그 출력
log() {
  local MSG="[$(date '+%Y-%m-%d %H:%M:%S')] $1"
  echo "$MSG"
}

log "===== Deployment script started ====="

trap 'echo "Error occured: $ERR_MSG. Exiting deploy script."; exit 1' ERR

if sudo docker ps --filter "name=app-blue" --quiet | grep -E .; then
  echo "Blue down, Green Up "
  BEFORE_COMPOSE_COLOR="blue"
  AFTER_COMPOSE_COLOR="green"
else
  echo "Green down, Blue up"
  BEFORE_COMPOSE_COLOR="green"
  AFTER_COMPOSE_COLOR="blue"
fi

# 새로운 image pull 받기
docker compose -f docker-compose.${AFTER_COMPOSE_COLOR}.yml pull dearbelly-api \
  || { echo "pull new image failed"; exit 1; }

# 새로운 서비스만 시작
docker compose -f docker-compose.${AFTER_COMPOSE_COLOR}.yml up -d --no-deps dearbelly-api \
  || { echo "bring up new service failed"; exit 1; }


sleep 10
echo "Switched from $BEFORE_COMPOSE_COLOR to $AFTER_COMPOSE_COLOR."

# 새로운 컨테이너가 제대로 떴는지 확인
if docker compose -p "$PROJECT" -f "docker-compose.${AFTER_COMPOSE_COLOR}.yml" ps \
     --services --filter status=running | grep -q "^dearbelly-api$"; then
  log "New container ($AFTER_COMPOSE_COLOR) is running. Reloading nginx..."
  sudo cp "/home/ubuntu/deploy/nginx/nginx.${AFTER_COMPOSE_COLOR}.conf" /etc/nginx/conf.d/default.conf
  sudo nginx -s reload

  # 이전 컨테이너 종료 (compose 네이밍 규칙에 맞게 down 권장)
  log "Stopping old stack ($BEFORE_COMPOSE_COLOR)..."
  docker compose -p "app-${BEFORE_COMPOSE_COLOR}" -f "docker-compose.${BEFORE_COMPOSE_COLOR}.yml" down || true
else
  log "New container is NOT running; keeping previous stack. Inspect logs."
  exit 1
fi

ERR_MSG="image prune failed"
docker image prune -af || true

log "Deployment completed successfully."
log "===== Deployment script ended ====="

exit 0