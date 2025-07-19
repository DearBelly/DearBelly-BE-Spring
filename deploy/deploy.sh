#!/bin/bash

set -e

ERR_MSG=''

trap 'echo "Error occured: $ERR_MSG. Exiting deploy script."; exit 1' ERR

if sudo docker ps --filter "name=dearbelly-api-blue" --quiet | grep -E .; then
  echo "blue up"
  docker compose -p dearbelly-api-blue -f docker-compose.blue.yml up -d
  BEFORE_COMPOSE_COLOR="green"
  AFTER_COMPOSE_COLOR="blue"
else
  echo "green up"
  docker compose -p dearbelly-api-green -f docker-compose.green.yml up -d
  BEFORE_COMPOSE_COLOR="blue"
  AFTER_COMPOSE_COLOR="green"
fi

sleep 10

echo "The $STOP_TARGET version is currently running on the server. Starting the $RUN_TARGET version."

# 새로운 컨테이너가 제대로 떴는지 확인
EXIST_AFTER=$(docker compose -p dearbelly-api-${AFTER_COMPOSE_COLOR} -f docker-compose.${AFTER_COMPOSE_COLOR}.yml ps | grep Up)
if [ -n "$EXIST_AFTER" ]; then
  # reload nginx
  sudo cp /home/ubuntu/nginx/nginx.${AFTER_COMPOSE_COLOR}.conf /etc/nginx/conf.d/default.conf
  sudo nginx -s reload

  # 이전 컨테이너 종료
  docker stop dearbelly-api-${BEFORE_COMPOSE_COLOR}
  docker rm dearbelly-api-${BEFORE_COMPOSE_COLOR}
  docker image prune -af
fi

echo "Deployment success."
exit 0
