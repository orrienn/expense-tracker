# Plan projektu: Expense Tracker

Aktualizuj ten plik po każdym skończonym zadaniu (`[ ]` → `[x]`).
Zaczynając sesję z agentem: *"Przeczytaj AGENTS.md i docs/PLAN.md, zajmiemy się: <punkt z listy>"*.

## Model danych
- **User**: id, email (unikalny), passwordHash, createdAt
- **Category**: id, name, user (właściciel)
- **Expense**: id, amount (BigDecimal), description, date, category, user, createdAt

Relacje: User 1—N Category, User 1—N Expense, Category 1—N Expense.

## Endpointy REST (`/api`)
| Metoda | Ścieżka | Opis |
|---|---|---|
| POST | /auth/register | rejestracja |
| POST | /auth/login | logowanie, zwraca JWT |
| GET | /users/me | dane zalogowanego użytkownika (id z tokena) |
| GET | /categories | lista kategorii użytkownika |
| POST | /categories | dodanie kategorii |
| PUT | /categories/{id} | edycja |
| DELETE | /categories/{id} | usunięcie |
| GET | /expenses | lista z filtrami: `from`, `to`, `categoryId`, `minAmount`, `maxAmount`, `sort`, `page` |
| POST | /expenses | dodanie wydatku |
| PUT | /expenses/{id} | edycja |
| DELETE | /expenses/{id} | usunięcie |
| GET | /stats/summary | suma w wybranym okresie, wydatki wg kategorii, wydatki wg miesięcy |

## Ekrany frontendu
- `/login`, `/register`
- `/dashboard`: karty z sumami (ten miesiąc, poprzedni), wykres kołowy wg kategorii, wykres słupkowy wg miesięcy
- `/expenses`: tabela z filtrami i sortowaniem, przyciski dodaj/edytuj/usuń (modal z formularzem)
- `/categories`: zarządzanie kategoriami

## Etapy

### Etap 0: Setup
- [x] Repo + `git init`, `.gitignore`, `CLAUDE.md`, ten plik
- [x] `docker-compose.yml` z PostgreSQL
- [x] Szkielet Spring Boot (Web, JPA, Security, Validation, PostgreSQL driver) + połączenie z bazą, H2 do testów
- [x] Szkielet React (Vite) + Tailwind + React Router
- [x] Zmienne środowiskowe w `.env` (poza repo), wzór w `.env.example`

### Etap 1: Autoryzacja
- [x] Encja User, repozytorium
- [x] Rejestracja (hashowanie hasła BCrypt, walidacja)
- [x] Logowanie + generowanie JWT
- [x] Weryfikacja JWT (wbudowany filtr resource server: `JwtDecoder` + `oauth2ResourceServer` w `SecurityConfig`)
- [x] Testy serwisu auth
- [x] Frontend: strony login/register, kontekst auth, chronione trasy

### Etap 2: Kategorie i wydatki (CRUD)
- [ ] Encje Category i Expense, DTO, mappery
- [ ] CRUD kategorii (tylko własne)
- [ ] CRUD wydatków (tylko własne, walidacja kwoty i daty)
- [ ] Globalna obsługa błędów (`@RestControllerAdvice`)
- [ ] Testy serwisów
- [ ] Frontend: strona kategorii
- [ ] Frontend: strona wydatków (lista + formularz dodawania/edycji)

### Etap 3: Filtrowanie i statystyki
- [ ] Filtrowanie, sortowanie i paginacja wydatków (backend)
- [ ] Frontend: panel filtrów i paginacja
- [ ] Endpoint `/stats/summary`
- [ ] Frontend: dashboard z wykresami (Recharts)

### Etap 4: Dopracowanie
- [ ] Obsługa stanów ładowania/błędów/pustych list na froncie
- [ ] Dockerfile dla backendu i frontendu, pełny `docker-compose up`
- [ ] Seed z przykładowymi danymi do demo
- [ ] README: opis, screenshoty, instrukcja uruchomienia, stack
- [ ] Wdrożenie (opcjonalnie): np. Render / Railway / Fly.io

## Pomysły na później (opcjonalne)
- Eksport wydatków do CSV
- Budżety miesięczne z ostrzeżeniem o przekroczeniu
- Wydatki cykliczne
- Ciemny motyw

## Notatki / decyzje
- (tu wpisuj ustalenia, np. dlaczego wybrano JWT w pamięci zamiast localStorage)
- Spring Boot 4 (nie 3): aktualna wersja, 3.x nie ma już darmowego wsparcia. Java 17 (minimum dla Boot 4).
- Backend czyta `../.env` przez `spring.config.import=optional:file:../.env[.properties]` (wbudowane w Springa,
  bez dodatkowej biblioteki). Zmienne środowiskowe systemu mają pierwszeństwo przed `.env`.
- Testy używają H2 w pamięci (`src/test/resources/application.properties`), więc nie potrzebują Dockera.
  Uwaga: H2 to nie Postgres, więc bardziej złożone zapytania (np. statystyki) trzeba sprawdzić też na Postgresie.
- Schemat bazy na razie przez `ddl-auto=update`; migracje (np. Flyway) rozważyć później.
- Tailwind CSS v4 przez plugin `@tailwindcss/vite` (bez `tailwind.config.js`; wystarczy `@import "tailwindcss"`).
- Frontend woła backend przez względne `/api/...`; w trybie dev proxy Vite przekazuje to na `localhost:8080`,
  więc na razie nie potrzebujemy konfiguracji CORS w Springu.
- Tabela użytkowników to `users`, bo `user` to słowo zarezerwowane w PostgreSQL.
- Bez Lomboka: gettery/settery w encjach piszemy ręcznie (zasada „bez nowych bibliotek bez pytania”).
- Email normalizowany: `trim()` w `RegisterRequest` (przed walidacją `@Email`), małe litery w `AuthService`.
- Hasło: 8–72 znaki (72 bajty to limit BCrypta). Rejestracja nie zwraca tokena, token daje dopiero `/login`.
- `SecurityConfig` na razie minimalny: CSRF wyłączony, sesje STATELESS, otwarte `/api/auth/**` i `/error`.
- `EmailAlreadyTakenException` tymczasowo z `@ResponseStatus(CONFLICT)`, do zastąpienia przez `@RestControllerAdvice` (Etap 2).
- JWT przez `spring-boot-starter-security-oauth2-resource-server` (oficjalny moduł Springa), a nie jjwt:
  bez własnego filtra, a `jjwt-jackson` używa Jacksona 2, podczas gdy Boot 4 ma Jacksona 3.
- Token: HS256, klucz `JWT_SECRET` z `.env` (Base64, min. 32 bajty), `sub` = id użytkownika, ważność `jwt.expiration` (domyślnie 1h).
  Bez refresh tokenów: po wygaśnięciu trzeba zalogować się ponownie.
- Logowanie: nieznany email i złe hasło dają ten sam błąd 401, żeby nie zdradzać, które konta istnieją.
- Brak/zły/przeterminowany token: 401 (a nie 403). Id zalogowanego użytkownika zawsze z tokena (`jwt.getSubject()`),
  nigdy z URL-a. W Etapie 2 wydzielić do tego wspólny helper dla kontrolerów.
- `SecurityIntegrationTest` (MockMvc + H2) pilnuje, że chronione endpointy wymagają poprawnego tokena.
- Limit hasła BCrypta to 72 **bajty**, nie znaki (np. „ą” = 2 bajty). Sprawdzane osobno w `RegisterRequest`
  (`@AssertTrue`) i w `validation.js`; bez tego 40 × „ą” dawało 500.
- Frontend: interfejs po angielsku. Token tylko w pamięci (`api/client.js` + `AuthProvider`), więc F5 wylogowuje.
  Na później: refresh token w ciasteczku `httpOnly`, żeby sesja przetrwała odświeżenie.
- Po rejestracji automatyczne logowanie. Przekierowania robią `GuestRoute`/`ProtectedRoute` (powrót na stronę z `state.from`).
- Odpowiedź 401 na żądanie z tokenem (np. wygasły token) → interceptor axios wylogowuje.
- `/.well-known/oauth-protected-resource`: publiczny opis API (RFC 9728) dodawany domyślnie przez Spring Security 7, bez poufnych danych.
