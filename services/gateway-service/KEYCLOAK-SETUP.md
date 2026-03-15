# Настройка Keycloak для gateway-service

- **"Client not found"** — в реалме нет клиента с таким `client-id`.  
- **"Invalid parameter: redirect_uri"** — в настройках клиента в **Valid redirect URIs** нет точного callback URL (см. п. 5 ниже).

## Что ожидает gateway (application-dev.yml)

| Параметр   | Значение |
|-----------|----------|
| Realm     | `microservices` |
| Client ID | `gateway-client` (или `oauth-client` — как в твоём конфиге) |
| Client secret | `changeIt` |
| Issuer (URL Keycloak) | `http://localhost:9080/realms/microservices` |

## Шаги в Keycloak Admin Console

1. **Открой Keycloak**  
   Обычно: `http://localhost:9080` (или тот порт, на котором запущен Keycloak).

2. **Создай realm (если ещё нет)**  
   - Realm name: `microservices`  
   - Create.

3. **Создай клиент**  
   - Clients → Create client  
   - **Client type:** OpenID Connect  
   - **Client ID:** `gateway-client` (точно как в application-dev.yml)  
   - Next.

4. **Capability config**  
   - **Client authentication:** ON (confidential client)  
   - **Authorization:** по желанию (можно выключить)  
   - **Authentication flow:** отметь **Standard flow** (Authorization code) и **Direct access grants** (нужен для получения токена в Postman через grant_type=password)  
   - Next.

5. **Login settings (обязательно для устранения "Invalid parameter: redirect_uri")**  
   - **Root URL:** `http://localhost:9000` (адрес gateway)  
   - **Valid redirect URIs** — добавь **точно** эти строки:  
     - `http://localhost:9000/login/oauth2/code/keycloak` (для входа через браузер/gateway)  
     - `https://oauth.pstmn.io/v1/callback` (если будешь получать токен через Postman OAuth 2.0 → Get New Access Token)  
     - при необходимости: `http://localhost:9000/*`, `http://localhost:8000/*`  
   - **Valid post logout redirect URIs:** `http://localhost:9000/*`, `http://localhost:8000/*`  
   - **Web origins:** `http://localhost:9000`, `http://localhost:8000`  
   - Save.

6. **Взять client secret**  
   - Открой клиент `gateway-client` → вкладка **Credentials**  
   - Скопируй **Secret**.  
   - Если он не `changeIt`, задай в конфиге gateway тот же секрет:
     - в `application-dev.yml`: `client-secret: <твой-secret>`,  
     - или переменная окружения.

7. **Проверь issuer**  
   - Realm settings → вкладка **General** → **Realm ID** должен быть `microservices`.  
   - В браузере открой `http://localhost:9080/realms/microservices` — должна открыться страница реалма (или .well-known).  
   - В gateway в `issuer-uri` должен быть именно этот URL (порт и хост как у твоего Keycloak).

После этого повтори запрос к gateway (например, к `/api/v1/orders/...`) — редирект на Keycloak должен вести на страницу входа, а не на "Client not found".

---

## Postman: как получить токен

**Способ A — один запрос (Password Grant)**  
1. В Keycloak у клиента включён **Direct access grants** (см. п. 4 выше).  
2. В Postman: POST `http://localhost:9080/realms/microservices/protocol/openid-connect/token`  
   Body (x-www-form-urlencoded): `grant_type=password`, `client_id=gateway-client`, `client_secret=...`, `username=...`, `password=...`  
3. В ответе взять `access_token` и в запросах к gateway указать Authorization → Bearer Token → вставить токен.

**Способ B — через браузер (Authorization Code)**  
1. В Keycloak в **Valid redirect URIs** добавлен `https://oauth.pstmn.io/v1/callback`.  
2. В Postman у запроса: Authorization → OAuth 2.0 → Grant Type: Authorization Code (with PKCE) → указать Auth URL, Access Token URL, Client ID, Client Secret → **Get New Access Token** → залогиниться в открывшемся браузере → Use Token.
