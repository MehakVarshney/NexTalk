# NexTalk Local Setup

This workspace currently has:

- `eureka-server` on port `8761`
- `api-gateway` on port `8080`
- `auth-service` on port `8081`
- PostgreSQL for auth on port `5432`

## Run With Docker Desktop

Start Docker Desktop first, then run:

```bash
docker compose up --build
```

Useful URLs:

- Eureka dashboard: `http://localhost:8761`
- Gateway Swagger UI: `http://localhost:8080/swagger-ui.html`
- Auth direct Swagger UI: `http://localhost:8081/swagger-ui.html`
- Auth through gateway: `http://localhost:8080/api/auth/register`

Stop services:

```bash
docker compose down
```

Stop and remove database volume:

```bash
docker compose down -v
```

## Run In STS

Import these Maven projects one by one:

1. `eureka-server`
2. `api-gateway`
3. `auth-service`

For STS local run, start PostgreSQL first. Easiest option:

```bash
docker compose up auth-db
```

Then run services in this order:

1. `EurekaServerApplication`
2. `AuthServiceApplication`
3. `ApiGatewayApplication`

Default auth database settings:

- URL: `jdbc:postgresql://localhost:5432/nextalk_auth`
- Username: `nextalk`
- Password: `nextalk`

## Sample Auth APIs

Register:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Mehak\",\"email\":\"mehak@example.com\",\"password\":\"secret123\"}"
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"mehak@example.com\",\"password\":\"secret123\"}"
```

Profile:

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

Update status:

```bash
curl -X PATCH http://localhost:8080/api/users/me/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d "{\"status\":\"ONLINE\"}"
```

Valid statuses are `ONLINE`, `AWAY`, and `OFFLINE`.

## Google Login

The endpoint is ready:

```text
POST /api/auth/google
```

Request body:

```json
{
  "idToken": "GOOGLE_ID_TOKEN"
}
```

Set `GOOGLE_CLIENT_ID` in Docker Compose or STS run configuration when you create a Google OAuth client.
