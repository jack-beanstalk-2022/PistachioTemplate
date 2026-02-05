#!/usr/bin/env tsx
/**
 * Rebrand script: replace package name, app name, project name, refactor directories.
 * Run from the project root.
 *
 * Usage: tsx rebrand.ts NEW_PROJECT_NAME NEW_PACKAGE
 *   e.g. tsx rebrand.ts PistachioTemplate com.pistachio.app
 */

import * as fs from "fs";
import * as path from "path";

// --- Config (fixed old values) ---
const OLD_PACKAGE = "com.jetbrains.kmpapp";
const OLD_APP_NAME = "KMP App";
const OLD_PROJECT_NAME = "KMP-App-Template";
const OLD_RES_PACKAGE_PREFIX = "kmp_app_template";

// Paths/dirs to skip when searching and replacing
const SKIP_PATTERNS = [
  /.git(\/|$)/,
  /\/build(\/|$)/,
  /\.gradle(\/|$)/,
  /composeApp\/build(\/|$)/,
  /\.(png|jpg|jpeg|gif|webp|ico|jar)$/i,
  /\.xcuserstate$/,
  /\.xcassets(\/|$)/,
  /\/xcuserdata\//,
  /rebrand\.(sh|ts)$/,
];

function shouldSkip(filePath: string, root: string): boolean {
  const relative = path.relative(root, filePath).replace(/\\/g, "/");
  return SKIP_PATTERNS.some((re) => re.test(relative));
}

function* walkDir(dir: string, root: string): Generator<string> {
  if (!fs.existsSync(dir)) return;
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const e of entries) {
    const full = path.join(dir, e.name);
    if (shouldSkip(full, root)) continue;
    if (e.isDirectory()) {
      yield* walkDir(full, root);
    } else if (e.isFile()) {
      yield full;
    }
  }
}

function filesContaining(root: string, needle: string): string[] {
  const out: string[] = [];
  for (const f of walkDir(root, root)) {
    try {
      const content = fs.readFileSync(f, "utf8");
      if (content.includes(needle)) out.push(f);
    } catch {
      // skip binary or unreadable
    }
  }
  return out;
}

function replaceInFiles(
  root: string,
  oldStr: string,
  newStr: string
): void {
  const files = filesContaining(root, oldStr);
  for (const f of files) {
    if (shouldSkip(f, root)) continue;
    try {
      let content = fs.readFileSync(f, "utf8");
      content = content.split(oldStr).join(newStr);
      fs.writeFileSync(f, content, "utf8");
      console.log("  updated:", path.relative(root, f));
    } catch (err) {
      console.error("  failed to update:", path.relative(root, f), err);
    }
  }
}

function refactorDirs(
  root: string,
  oldPackage: string,
  newPackage: string
): void {
  console.log("Refactoring package directories...");
  const oldPath = oldPackage.replace(/\./g, path.sep);
  const newPath = newPackage.replace(/\./g, path.sep);

  const kotlinBases = [
    "composeApp/src/commonMain/kotlin",
    "composeApp/src/androidMain/kotlin",
    "composeApp/src/iosMain/kotlin",
    "composeApp/src/androidInstrumentedTest/kotlin",
  ];

  for (const base of kotlinBases) {
    const baseFull = path.join(root, base);
    const oldDir = path.join(baseFull, oldPath);
    const newDir = path.join(baseFull, newPath);

    if (!fs.existsSync(oldDir) || !fs.statSync(oldDir).isDirectory()) continue;

    fs.mkdirSync(newDir, { recursive: true });
    const entries = fs.readdirSync(oldDir, { withFileTypes: true });
    for (const e of entries) {
      const src = path.join(oldDir, e.name);
      const dest = path.join(newDir, e.name);
      fs.renameSync(src, dest);
    }

    // Remove old empty directories
    let current = oldPath;
    while (current && current !== "." && current !== path.sep) {
      const toRemove = path.join(baseFull, current);
      try {
        fs.rmdirSync(toRemove);
      } catch {
        break;
      }
      current = path.dirname(current);
    }

    console.log(
      "  moved contents:",
      path.join(base, oldPath),
      "->",
      path.join(base, newPath)
    );
  }
}

function main(): void {
  const args = process.argv.slice(2);
  if (args.length < 2) {
    console.error("Usage: npx ts-node rebrand.ts NEW_PROJECT_NAME NEW_PACKAGE");
    console.error("  e.g. npx ts-node rebrand.ts PistachioTemplate com.pistachio.app");
    process.exit(1);
  }

  const [newProjectName, newPackage] = args;

  // Validate NEW_PACKAGE: Java/Kotlin package = dot-separated identifiers
  const packageRe = /^[a-zA-Z_][a-zA-Z0-9_]*(\.[a-zA-Z_][a-zA-Z0-9_]*)+$/;
  if (!packageRe.test(newPackage)) {
    console.error("Error: Invalid package name:", newPackage);
    console.error(
      "Package must contain at least one '.' (e.g. com.example.myapp). Each part must start with a letter or underscore."
    );
    process.exit(1);
  }

  const newAppName = newProjectName.replace(/-/g, " ");
  const newResPackagePrefix = newProjectName
    .toLowerCase()
    .replace(/-/g, "_");

  const root = path.resolve(__dirname);
  process.chdir(root);

  console.log(
    `Rebranding: ${OLD_PACKAGE} -> ${newPackage}, "${OLD_APP_NAME}" -> "${newAppName}", ${OLD_PROJECT_NAME} -> ${newProjectName}, ${OLD_RES_PACKAGE_PREFIX} -> ${newResPackagePrefix}`
  );

  console.log("Replacing package name...");
  replaceInFiles(root, OLD_PACKAGE, newPackage);

  console.log("Replacing app name...");
  replaceInFiles(root, OLD_APP_NAME, newAppName);

  console.log("Replacing project name...");
  replaceInFiles(root, OLD_PROJECT_NAME, newProjectName);

  console.log("Replacing Compose resources package prefix...");
  replaceInFiles(root, OLD_RES_PACKAGE_PREFIX, newResPackagePrefix);

  refactorDirs(root, OLD_PACKAGE, newPackage);

  console.log("Rebrand done.");
}

main();
