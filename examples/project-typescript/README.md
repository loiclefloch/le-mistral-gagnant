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

## API Guideline Linter

This project includes an **API Guideline Linter** that analyzes your codebase for REST API design violations. The linter is located in the `../linter` directory.

### Quick Start

#### Run the Linter
From the project root directory, execute the linter against your source code:

```bash
# Lint the src directory
node ../linter/api_guidelines_linter.js ./src

# Lint the entire project
node ../linter/api_guidelines_linter.js .

# Lint a specific file
node ../linter/api_guidelines_linter.js ./src/infrastructure/express-server.ts
```
## Purpose
Use this project to see how the linter detects violations of the API guidelines.

## 📊 Audit Dashboard (Docusaurus)

The project includes an interactive audit dashboard built with Docusaurus that visualizes API audit reports and improvement recommendations.

### Prerequisites
- Node.js 16+ installed
- npm or yarn package manager

### Installation

Navigate to the audit dashboard directory:
```bash
cd audit-dashboard
npm install
```

### Running the Dashboard

**Development Mode** (with hot reload):
```bash
npm run start
```
The dashboard will be available at `http://localhost:3000/`

**Production Build**:
```bash
npm run build
```

This generates optimized static files in the `build/` directory.

**Serve Production Build**:
```bash
npm run serve
```

### Dashboard Features
- 📈 **Detailed Scoring**: API audit scores by domain
- 🔴 **Critical Issues**: Top problems and severity levels
- 🚀 **Action Plan**: Prioritized improvements with timeline
- 📊 **Graphs & Charts**: Visual representation of audit results
- ⏱️ **Timeline**: Implementation schedule for fixes

### Structure
The audit dashboard content is located in:
- `audit-dashboard/docs/audit/` - Audit report content
- `audit-dashboard/docs/` - Documentation pages
- `audit-dashboard/sidebars.ts` - Navigation configuration

