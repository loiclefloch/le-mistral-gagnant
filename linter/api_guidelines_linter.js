#!/usr/bin/env node
/**
 * API Guideline Linter
 * Checks API codebase for violations of RESTful API design guidelines.
 * Language agnostic: works by scanning source files and extracting endpoint definitions, parameters, and documentation.
 * Usage: node api_guideline_linter.js <path-to-api-source>
 */
const fs = require('fs');
const path = require('path');

const GUIDELINE_VIOLATIONS = {
  url_naming: /\/(get|set|update|delete)[A-Z]/g,
  trailing_slash: /\/\w+\/$/gm,
  versioning: /\/v\d+\/$/gm,
  resource_nesting: /(\/\w+){3,}/g,
  mixed_case: /\b[a-z]+_[A-Z][a-z]+\b|\b[A-Z][a-z]+_[a-z]+\b/g,
  http_method_in_url: /\/\w+(GET|POST|PUT|PATCH|DELETE)/g,
  error_format: /error\s*[:=]\s*"?\w+"?/g,
};

const RULES_INFO = {
  url_naming: {
    title: 'Verb-like segments in URL',
    description:
      'URLs should represent resources (nouns), not actions. Avoid using verbs like `get`, `set`, `update`, or `delete` in path segments.',
    suggestion: 'Rename the path to use a resource and HTTP method; e.g. `GET /users` instead of `/getUser`.',
    example: '`/users/{id}` instead of `/getUser/{id}`',
  },
  trailing_slash: {
    title: 'Trailing slash in resource path',
    description:
      'Prefer consistent usage of trailing slashes. Typically APIs avoid trailing slashes for resource endpoints.',
    suggestion: 'Remove the trailing slash or normalize it across the API.',
    example: '`/users` instead of `/users/`',
  },
  versioning: {
    title: 'Versioning at the end of the path',
    description:
      'API version identifiers should be part of the base path (e.g. `/v1`) and not used as a trailing segment on resource endpoints.',
    suggestion: 'Move `v1` to the base path: e.g. `/v1/users`.',
    example: '`/v1/users`',
  },
  resource_nesting: {
    title: 'Deep resource nesting',
    description:
      'Avoid deeply nested resources; prefer flatter structures or an association resource when appropriate.',
    suggestion: 'Consider using query parameters or separate endpoints for deeply nested data.',
    example: '`/users/{id}/orders` instead of `/users/{id}/accounts/{aid}/orders/{oid}`',
  },
  mixed_case: {
    title: 'Mixed or inconsistent casing',
    description:
      'URLs and identifiers should use consistent casing (kebab-case or snake_case). MixedCase is confusing and error-prone.',
    suggestion: 'Use kebab-case (recommended) or snake_case consistently across endpoints.',
    example: '`/user-profiles`',
  },
  http_method_in_url: {
    title: 'HTTP method included in URL',
    description:
      "Don't encode the HTTP method into the URL. Use the correct HTTP verb (GET/POST/PUT/etc.) instead.",
    suggestion: 'Remove method from URL and use HTTP verb: `POST /users` to create a user.',
    example: '`POST /users` (not `/createUserPOST`)',
  },
  error_format: {
    title: 'Inconsistent error key format',
    description:
      'Errors should follow a consistent structured format (e.g. JSON object with `code`, `message`, and optional `details`).',
    suggestion: 'Return structured error objects and document the schema.',
    example: '`{ "code": "USER_NOT_FOUND", "message": "User not found" }`',
  },
};

const REPORT = {};

// Files and directories to ignore while scanning
const IGNORED_DIRS = new Set([
  'node_modules',
  '.git',
  '.github',
  '.vscode',
  'dist',
  'build',
]);

const IGNORED_FILES = new Set([
  'package-lock.json',
  'yarn.lock',
  'pnpm-lock.yaml',
  '.env',
  '.env.local',
  '.gitignore',
  '.gitattributes',
  '.eslintrc',
  '.eslintrc.js',
  '.eslintrc.json',
  '.prettierrc',
  '.prettierrc.json',
  '.editorconfig',
  'Dockerfile',
  'docker-compose.yml',
]);

// file extensions we'll scan for (still keep language-agnostic list)
const SCAN_FILE_EXTENSIONS = /\.(js|ts|py|java|go|rb|php|cs|yaml|yml|json)$/i;

function scanFile(filepath) {
  const content = fs.readFileSync(filepath, 'utf8');
  const lines = content.split(/\r?\n/);
  for (const [rule, pattern] of Object.entries(GUIDELINE_VIOLATIONS)) {
    let violations = [];
    lines.forEach((line, idx) => {
      const matches = line.match(pattern);
      if (matches && matches.length > 0) {
        matches.forEach(match => {
          violations.push({ match, line: idx + 1, snippet: line.trim() });
        });
      }
    });
    if (violations.length > 0) {
      if (!REPORT[rule]) REPORT[rule] = [];
      REPORT[rule].push({ filepath, violations });
    }
  }
}

function scanDirectory(directory) {
  fs.readdirSync(directory, { withFileTypes: true }).forEach(dirent => {
    const fullPath = path.join(directory, dirent.name);
    // skip ignored directories
    if (dirent.isDirectory()) {
      if (IGNORED_DIRS.has(dirent.name)) return;
      // also skip hidden folders except those explicitly allowed
      if (dirent.name.startsWith('.') && !['.github', '.vscode'].includes(dirent.name)) return;
      scanDirectory(fullPath);
    } else if (dirent.isFile()) {
      // skip known config/lock files
      if (IGNORED_FILES.has(dirent.name)) return;
      // skip dotfiles (hidden config files)
      if (dirent.name.startsWith('.')) return;
      // only scan files with allowed extensions
      if (!SCAN_FILE_EXTENSIONS.test(dirent.name)) return;
      scanFile(fullPath);
    }
  });
}

function printReport() {
  console.log('\nAPI Guideline Linter Report');
  console.log('='.repeat(32));
  if (Object.keys(REPORT).length === 0) {
    console.log('No guideline violations found.');
    return;
  }
  let total = 0;
  for (const [rule, violations] of Object.entries(REPORT)) {
    const info = RULES_INFO[rule] || {};
    console.log(`\nRule: ${rule} - ${info.title || 'Guideline violation'}`);
    if (info.description) console.log(`  Description: ${info.description}`);
    if (info.suggestion) console.log(`  Suggestion: ${info.suggestion}`);
    if (info.example) console.log(`  Example: ${info.example}`);

    violations.forEach(({ filepath, violations }) => {
      console.log(`\n  File: ${filepath}`);
      const unique = new Map();
      violations.forEach(({ match, line, snippet }) => {
        const key = `${match}@${line}@${snippet}`;
        if (!unique.has(key)) {
          unique.set(key, true);
          total += 1;
          console.log(`    • Line ${line}: ${snippet}`);
          console.log(`      Matched: "${match}"`);
        }
      });
    });
  }
  console.log(`\nTotal violations: ${total}`);
}

function main() {
  if (process.argv.length !== 3) {
    console.log('Usage: node api_guideline_linter.js <path-to-api-source>');
    process.exit(1);
  }
  const inputPath = process.argv[2];
  if (!fs.existsSync(inputPath)) {
    console.log(`Error: ${inputPath} does not exist.`);
    process.exit(1);
  }
  const stat = fs.statSync(inputPath);
  if (stat.isDirectory()) {
    scanDirectory(inputPath);
  } else if (stat.isFile()) {
    scanFile(inputPath);
  } else {
    console.log(`Error: ${inputPath} is not a file or directory.`);
    process.exit(1);
  }
  printReport();
}

if (require.main === module) {
  main();
}
