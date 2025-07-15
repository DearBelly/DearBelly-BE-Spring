#!/bin/bash
IS_GREEN=$(docker ps | grep green)
DEFAULT_CONF="/etc/nginx/ngixn.conf"

if [ -z $IS_GREEN ];then

  echo "### BLUE -> GREEN ###"
  echo "1. get green image from docker"
  docker pull ${ECR_URI}/dearbelly-api:green

  echo "2. green container up"
  docker-compose up -d ${ECR_URI}/dearbelly-api:green

  while [ 1 = 1 ]; do
  echo "3. green health check"
  sleep 3

  REQUEST=$(curl http://127.0.0.1:8080)
    if [ -n "$REQUEST" ]; then
        echo "health check is Success"
        break ;
    fi
  done;

  echo "4. reload nginx"
  sudo cp /etc/nginx/nginx.green.conf /etc/nginx/nginx.conf
  sudo nginx -s reload

  echo "5. blue container down"
  docker compose stop blue
else
  echo "### GREEN -> BLUE ###"
  echo "1. get blue image from docker"
  docker pull ${ECR_URI}/dearbelly-api:blue

  echo "2. blue container up"
  docker-compose up -d ${ECR_URI}/dearbelly-api:blue

  while [ 1 = 1 ]; do
  echo "3. blue health check"
  sleep 3
  REQUEST=$(curl http://127.0.0.1:8081)
    if [ -n "$REQUEST" ]; then
        echo "health check is Success"
        break ;
    fi
  done;

  echo "4. reload nginx"
  sudo cp /etc/nginx/nginx.blue.conf /etc/nginx/nginx.conf
  sudo nginx -s reload

  echo "5. blue container down"
  docker compose stop blue
fi