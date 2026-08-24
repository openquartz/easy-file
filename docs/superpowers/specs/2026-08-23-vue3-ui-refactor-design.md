# Vue 3 + Element Plus Admin UI Refactor

Date: 2026-08-23  
Branch: `feature/ui_refactor-260823` (from `master`)  
Status: Approved in design review

## 1. Problem

`easyfile-ui` ships a Thymeleaf + Layui console (login shell, iframe layout, download-task table). The stack is hard to evolve, and the requested direction is Vue 3 + Element Plus while remaining an embeddable Maven module.

## 2. Goals

- Replace the admin frontend with an embedded Vue 3 SPA (Element Plus, TypeScript, Vue Router, axios).
- Keep Cookie login (`EF_LOGIN_IDENTITY`) and existing JSON APIs under `/easyfile-ui/`.
- Feature parity: login, layout, download list, download / retry / revoke, APP switch.
- Visual: macOS-like light white + pale blue (not Layui black sidebar, not default Element admin chrome).
- `mvn -pl easyfile-ui package` still produces a jar that serves the console.

## 3. Non-goals

- Import-task admin, date filters, extra dashboards.
- Independent frontend port / CORS-first deployment.
- Changing the Cookie auth model or admin credential source.
- Changing download-storage business APIs.

## 4. Decisions

| Topic | Choice |
|---|---|
| Hosting | Embedded SPA in `easyfile-ui/frontend`, Vite build into classpath |
| Router | `createWebHistory('/easyfile-ui/')` with only `/` and `/toLogin` |
| State | No Pinia; composables + API modules |
| Element Plus | Full import + `zh-cn` locale; theme via CSS variables |
| Visual | macOS light: page `#F2F5F9`, accent `#5BA3E8`, frosted header, light sidebar |
| `showAppId` | New `POST /easyfile-ui/download-task/ui-config` |
| Old `/download-task/get` | Redirect to `/easyfile-ui/` |
| Logout | Clear cookie, redirect to `/easyfile-ui/toLogin` |
| Build | `frontend-maven-plugin` Node 20 on `prepare-package` |
| Built assets | Not committed; generated into `easyfile-admin-ui/` |

## 5. Architecture

Browser loads `GET /easyfile-ui/` or `/toLogin` (Spring forwards `index.html`). JSON stays on `POST /easyfile-ui/login` and `/easyfile-ui/download-task/*`. Unauthenticated `@Auth` page hits still 302 to `/easyfile-ui/toLogin`.

Local Vite (`:5173`) proxies login/logout/download-task to Spring (`:8080`).

## 6. Frontend

- Routes: `/toLogin` → LoginView; `/` → AdminLayout + DownloadTaskView.
- Login posts `{ username, password, rememberMe: 'on' }`; success goes to `/easyfile-ui/`.
- Table columns and actions match the former Layui page. Retry only when `uploadStatus === 'fail'`. APP switch appends `?unifiedAppId=`.
- Theme tokens live in `frontend/src/styles/theme.css`.

## 7. Backend

- `IndexController`: `/` and `/toLogin` forward SPA; logout redirects to toLogin.
- `DownloadTaskController.get` redirects home; add `ui-config`.
- Remove Thymeleaf, Layui, old templates/JS/CSS.
- Keep `AuthInterceptor` 302 behavior.

## 8. Test plan

- Existing `DownloadTaskControllerRetryTest`.
- Controller tests: `ui-config`, SPA forward/redirect, logout redirect.
- Vitest for `ResponseResult` unwrap.
- Browser: login, list, actions, APP switch, unauth 302, macOS light theme.
