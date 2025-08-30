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

# image 변경 사항 pull 받기
ERR_MSG="pull new image failed"
docker compose -p app-${AFTER_COMPOSE_COLOR} -f docker-compose.${AFTER_COMPOSE_COLOR}.yml \
  pull dearbelly-api

# 새로운 서비스만 시작하기(의존성 건드리지 않음)
ERR_MSG="bring up new service failed"
docker compose -p app-${AFTER_COMPOSE_COLOR} -f docker-compose.${AFTER_COMPOSE_COLOR}.yml \
  up -d --no-deps --force-recreate dearbelly-api

sleep 10
log "Switched from $BEFORE_COMPOSE_COLOR to $AFTER_COMPOSE_COLOR."

# 새로운 컨테이너가 제대로 떴는지 확인
EXIST_AFTER=$(docker compose -p app-${AFTER_COMPOSE_COLOR} -f docker-compose.${AFTER_COMPOSE_COLOR}.yml ps | grep Up)
if [ -n "$EXIST_AFTER" ]; then
  # reload nginx
  log "New container ($AFTER_COMPOSE_COLOR) is running. Reloading nginx..."
  sudo cp /home/ubuntu/nginx/nginx.${AFTER_COMPOSE_COLOR}.conf /etc/nginx/conf.d/default.conf
  sudo nginx -s reload

  # 이전 컨테이너 종료
  log "Stopping old container ($BEFORE_COMPOSE_COLOR)..."
  if docker ps -a --format '{{.Names}}' | grep -q "^app-${BEFORE_COMPOSE_COLOR}\$"; then
    log "Found old container app-${BEFORE_COMPOSE_COLOR}, stopping and removing..."
    docker stop app-${BEFORE_COMPOSE_COLOR}
    docker rm app-${BEFORE_COMPOSE_COLOR}
  else
    log "No existing container named app-${BEFORE_COMPOSE_COLOR}. Skipping down."
  fi
fi

ERR_MSG="image prune failed"
docker image prune -af || true

log "Deployment completed successfully."
log "===== Deployment script ended ====="

exit 0