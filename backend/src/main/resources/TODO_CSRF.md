# TODO: Re-enable CSRF protection

Currently disabled in `SecurityConfig.java` (`.csrf(AbstractHttpConfigurer::disable)`), flagged by
SonarCloud (java:S4502, Critical). Relevant because auth is session/cookie-based
(`SessionCreationPolicy.ALWAYS` + GitHub OAuth2 login), not Bearer tokens — the classic CSRF
scenario. No frontend code exists yet (`frontend/src` is still just the Vite scaffold), so this is
easiest to do now, before any Axios/fetch calls are written against the API.

Three places to touch:

## 1. Backend — `backend/src/main/java/ropold/backend/security/SecurityConfig.java`

Replace the `.csrf(AbstractHttpConfigurer::disable)` line in `filterChain(...)` with a cookie-based
CSRF token repository so a JS frontend can read the token:

```java
.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
```

Import: `org.springframework.security.web.csrf.CookieCsrfTokenRepository`.

GET requests are unaffected (CSRF only applies to POST/PUT/DELETE/PATCH by default), so the
existing `permitAll()` GET rules keep working as-is.

## 2. Backend — CORS / cookie settings (if frontend ends up on a different origin than the API)

If frontend and backend are served from different origins (likely, given `app.url` is a separate
property from the API's own host), cross-site cookies need:

- A `CorsConfigurationSource` bean with `setAllowCredentials(true)` and an explicit allowed origin
  (not `"*"` — credentialed CORS requires a concrete origin).
- `server.servlet.session.cookie.same-site=None` in `application.properties` (requires HTTPS/
  `Secure` cookies, which Render already provides).

If frontend and backend end up served from the same origin (e.g. reverse-proxied), skip this step.

## 3. Frontend — Axios (or fetch) config, once frontend code exists

Axios has built-in support for the `CookieCsrfTokenRepository` pattern: it automatically reads a
cookie named `XSRF-TOKEN` and sends it back as the `X-XSRF-TOKEN` header on mutating requests —
this matches Spring Security's default header/cookie names, so no backend-side renaming is needed.

```ts
axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // required to send/receive the session + XSRF-TOKEN cookies at all
});
```

`withCredentials: true` is required both for the session cookie (login) and the CSRF cookie to be
sent on cross-origin requests. Without it, the browser won't attach either cookie and every
mutating request will start failing with 403 (missing CSRF token) once step 1 is live.

## Suggested order

1. Do step 1 (backend) and step 2 (CORS/cookie, if needed) together, then verify with a manual
   `curl`/Postman flow (login, grab `XSRF-TOKEN` cookie, send it back as a header on a POST) before
   writing any frontend code.
2. Do step 3 once the frontend starts making real requests, so `withCredentials` is baked in from
   the first API call instead of retrofitted later.