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

const REPORT = {};

function scanFile(filepath) {
  const content = fs.readFileSync(filepath, 'utf8');
  const lines = content.split(/\r?\n/);
  for (const [rule, pattern] of Object.entries(GUIDELINE_VIOLATIONS)) {
    let violations = [];
    lines.forEach((line, idx) => {
      const matches = line.match(pattern);
      if (matches && matches.length > 0) {
        matches.forEach(match => {
          violations.push({ match, line: idx + 1 });
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
    if (dirent.isDirectory()) {
      scanDirectory(fullPath);
    } else if (dirent.isFile() && /\.(js|ts|py|java|go|rb|php|cs|yaml|yml|json)$/.test(dirent.name)) {
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
  for (const [rule, violations] of Object.entries(REPORT)) {
    console.log(`\nRule: ${rule}`);
    violations.forEach(({ filepath, violations }) => {
      console.log(`  File: ${filepath}`);
      const unique = new Map();
      violations.forEach(({ match, line }) => {
        const key = `${match}@${line}`;
        if (!unique.has(key)) {
          unique.set(key, true);
          console.log(`    Violation: ${match} (line ${line})`);
        }
      });
    });
  }
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
