# Expense Tracker

> Plik z instrukcjami dla agenta. Nazwij go zależnie od narzędzia:
> `AGENTS.md` (uniwersalnie), `CLAUDE.md` (Claude Code) albo `.github/copilot-instructions.md` (Copilot).

## O projekcie
Full-stack aplikacja do śledzenia wydatków. Użytkownik rejestruje się, loguje i zarządza
własnymi wydatkami oraz kategoriami, filtruje je i ogląda statystyki na dashboardzie.
Projekt portfolio, pisany krok po kroku. Aktualny plan i stan prac: `docs/PLAN.md`.

## Stack
- Backend: Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security (JWT), Bean Validation
- Baza: PostgreSQL (lokalnie przez Docker Compose); H2 tylko do testów
- Frontend: React + Vite, React Router, axios, Recharts, Tailwind CSS
- Testy: JUnit 5, Mockito (backend); opcjonalnie Vitest (frontend)
- Narzędzia: Maven, Docker Compose, Git

## Struktura repo
```
/backend     Spring Boot (pakiety: config, controller, service, repository, model, dto, security, exception)
/frontend    React (src: api, components, pages, context, hooks)
/docs        PLAN.md i inne notatki
docker-compose.yml
```

## Komendy
- Baza: `docker compose up -d db`
- Backend: `cd backend && ./mvnw spring-boot:run`
- Testy backendu: `cd backend && ./mvnw test`
- Frontend: `cd frontend && npm install && npm run dev`

## Zasady dla backendu
- Architektura warstwowa: controller → service → repository. Logika tylko w serwisach.
- Kontrolery zwracają DTO, nigdy encji JPA.
- Każdy endpoint (poza `/api/auth/**`) wymaga JWT. Użytkownik widzi i modyfikuje wyłącznie swoje dane
  (zawsze filtruj po zalogowanym użytkowniku).
- Walidacja wejścia przez adnotacje (`@Valid`, `@NotNull`, `@Positive` itd.).
- Jeden globalny `@RestControllerAdvice` do obsługi błędów, spójny format odpowiedzi błędu.
- Kwoty jako `BigDecimal`, nigdy `double`.
- Do każdego serwisu piszemy testy jednostkowe.

## Zasady dla frontendu
- Komponenty funkcyjne + hooki. Wywołania API wyłącznie w `src/api`.
- Token JWT trzymamy w pamięci aplikacji/kontekście (nie logujemy go do konsoli).
- Obsługuj stany: ładowanie, błąd, pusta lista.
- Formularze z walidacją po stronie klienta (uzupełnia, nie zastępuje walidacji backendu).

## Zasady pracy
- Najpierw plan, potem kod: przy większych zadaniach najpierw zaproponuj plan i poczekaj na akceptację.
- Małe kroki: jedno zadanie z `docs/PLAN.md` na raz.
- Nie dodawaj nowych bibliotek bez pytania.
- Nie zmieniaj plików niezwiązanych z zadaniem.
- Tłumacz krótko, co zmieniłeś i dlaczego (użytkownik uczy się na tym kodzie).
- Sekrety (hasła, klucze JWT) tylko w `.env` / zmiennych środowiskowych, nigdy w repo.
- Po skończonym zadaniu zaznacz je w `docs/PLAN.md` i zaproponuj treść commita.
