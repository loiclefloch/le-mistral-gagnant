# Bad API Demo Project

This project demonstrates how the API guidelines linter works by intentionally violating best practices in API design. It uses a hexagonal architecture and exposes several endpoints with common mistakes.

## Structure
- `src/domain`: Domain models
- `src/application`: Application services
- `src/infrastructure`: Express server and endpoints
- `test`: Basic tests for endpoints

## How to Run
1. Install dependencies: `npm install`
2. Start the server: `npm start`
3. Run tests: `npm test`

## Endpoints
- `POST /addUser` — Adds a user (no validation, poor naming)
- `GET /getUser/:id` — Gets a user by ID (no error handling)
- `GET /allUsers` — Lists all users (no pagination)
- `DELETE /removeUser/:id` — Removes a user (no authentication)

## Purpose
Use this project to see how the linter detects violations of the API guidelines.
