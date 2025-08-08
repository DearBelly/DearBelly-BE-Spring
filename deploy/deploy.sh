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

if sudo docker ps --filter "name=dearbelly-api-blue" --quiet | grep -E .; then
  echo "Blue down, Green Up "
  docker compose -p dearbelly-api-green -f docker-compose.green.yml up -d
  BEFORE_COMPOSE_COLOR="blue"
  AFTER_COMPOSE_COLOR="green"
else
  echo "Green down, Blue up"
  docker compose -p dearbelly-api-blue -f docker-compose.blue.yml up -d
  BEFORE_COMPOSE_COLOR="green"
  AFTER_COMPOSE_COLOR="blue"
fi

sleep 10
log "Switched from $BEFORE_COMPOSE_COLOR to $AFTER_COMPOSE_COLOR."

# 새로운 컨테이너가 제대로 떴는지 확인
EXIST_AFTER=$(docker compose -p dearbelly-api-${AFTER_COMPOSE_COLOR} -f docker-compose.${AFTER_COMPOSE_COLOR}.yml ps | grep Up)
if [ -n "$EXIST_AFTER" ]; then
  # reload nginx
  log "New container ($AFTER_COMPOSE_COLOR) is running. Reloading nginx..."
  sudo cp /home/ubuntu/nginx/nginx.${AFTER_COMPOSE_COLOR}.conf /etc/nginx/conf.d/default.conf
  sudo nginx -s reload

  # 이전 컨테이너 종료
  log "Stopping old container ($BEFORE_COMPOSE_COLOR)..."
  docker stop dearbelly-api-${BEFORE_COMPOSE_COLOR}
  docker rm dearbelly-api-${BEFORE_COMPOSE_COLOR}
  docker image prune -af
else
  log "ERROR: New container ($AFTER_COMPOSE_COLOR) failed to start."
  exit 1
fi

log "Deployment completed successfully."
log "===== Deployment script ended ====="

exit 0