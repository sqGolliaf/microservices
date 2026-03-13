#!/bin/bash

# ============================
# Настройки
# ============================
KEYCLOAK_URL="http://localhost:9080/realms/microservices/protocol/openid-connect/token"
CLIENT_ID="gateway-client"
USERNAME="user"
PASSWORD="admin"

API_URL="http://localhost:9000/api/v1/onto"

# ============================
# Получаем токен
# ============================
echo "Получаем токен из Keycloak..."
RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=$CLIENT_ID" \
  -d "username=$USERNAME" \
  -d "password=$PASSWORD")

# Извлекаем access_token без jq
ACCESS_TOKEN=$(echo "$RESPONSE" | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"\(.*\)"/\1/')

# Проверяем, получили ли токен
if [ -z "$ACCESS_TOKEN" ]; then
  echo "Ошибка: не удалось получить токен"
  echo "Ответ Keycloak: $RESPONSE"
  exit 1
fi

echo "Токен получен: $ACCESS_TOKEN"
echo

# ============================
# Делаем запрос к API с токеном
# ============================
echo "Делаем запрос к API..."
curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$API_URL" | python -m json.tool