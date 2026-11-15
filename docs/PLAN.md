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
- [x] Encje Category i Expense, DTO, mappery
- [x] CRUD kategorii (tylko własne)
- [x] CRUD wydatków (tylko własne, walidacja kwoty i daty)
- [x] Globalna obsługa błędów (`@RestControllerAdvice`)
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
- Relacje tylko `@ManyToOne(fetch = LAZY)` (jednokierunkowe): `User` nie ma list kategorii/wydatków, pobieramy je przez repozytoria.
- Mappery bez MapStruct: statyczne `XxxResponse.from(encja)`. `ExpenseResponse.from()` czyta leniwą kategorię,
  więc wywołujemy go tylko w metodach `@Transactional` serwisu (`open-in-view=false`).
- Kategoria wydatku opcjonalna (`null` = „Uncategorized”). Usunięcie kategorii ustawia `category = null`
  w jej wydatkach (`ExpenseRepository.clearCategory`, jeden `UPDATE`), nic nie jest kasowane kaskadowo.
- Nazwa kategorii unikalna w obrębie użytkownika (`UNIQUE(user_id, name)`), maks. 50 znaków, `trim()` w `CategoryRequest`.
- Wydatek: kwota `numeric(12,2)` + `@Positive @Digits(10, 2)`, data `@PastOrPresent` (bez przyszłych),
  opis opcjonalny (maks. 255, pusty → `null`). Kolumna daty to `expense_date`, bo `DATE` to słowo kluczowe SQL.
- Id zalogowanego użytkownika w kontrolerach: `@CurrentUserId Long userId` (meta-adnotacja na `@AuthenticationPrincipal`
  z wyrażeniem SpEL zamieniającym `sub` z tokena na `Long`).
- Cudzy zasób → 404 (jak nieistniejący), nie 403, żeby nie zdradzać, że istnieje. Repozytoria zawsze filtrują po `userId`.
- Duplikat nazwy kategorii sprawdzany bez względu na wielkość liter (w serwisie) → 409. Ograniczenie `UNIQUE` w bazie
  rozróżnia wielkość liter, więc przy równoczesnych żądaniach teoretycznie mogą powstać „Food” i „food”.
- Integracyjne testy na H2 dzielą jedną bazę (wspólny kontekst Springa), więc używają unikalnych emaili (UUID).
- `categoryId` z treści żądania wydatku sprawdzany przez `findByIdAndUserId`: cudza/nieistniejąca kategoria → 404
  (ten sam `CategoryNotFoundException`). Bez tego dałoby się podpiąć wydatek pod cudzą kategorię.
- `PUT /expenses/{id}` podmienia wszystkie pola; `categoryId = null` odpina kategorię.
- Lista wydatków: `@EntityGraph(attributePaths = "category")`, żeby kategorie przyszły w jednym zapytaniu (JOIN),
  a nie osobnym SELECT-em na każdy wydatek (problem N+1). Na razie pełna lista od najnowszych; paginacja w Etapie 3.
- Błędy API w formacie `ProblemDetail` (RFC 9457, `application/problem+json`): `type`, `title`, `status`, `detail`, `instance`;
  przy walidacji dodatkowo `errors: {pole: komunikat}`. `GlobalExceptionHandler` dziedziczy po `ResponseEntityExceptionHandler`
  (obsługuje błędy samego Springa: zły JSON, zły typ w URL-u, 405...). Własne wyjątki dziedziczą po `NotFoundException` (404)
  albo `ConflictException` (409). Nieoczekiwany wyjątek → 500 z ogólnym komunikatem, szczegóły tylko w logach.
- 401 przy braku/złym tokenie zwraca filtr Spring Security (pusta treść + nagłówek `WWW-Authenticate`, standard OAuth2),
  a nie `@RestControllerAdvice`. Na razie zostawiamy; ewentualnie później własny `AuthenticationEntryPoint` z `ProblemDetail`.
