# TypeScript Monorepo Module Creation Prompt

## Introduction

This guide explains how to create new modules in a TypeScript monorepo that follows a specific architectural pattern. Both the AI assistant and the user must understand these core concepts to successfully perform module creation tasks.

### Core Concepts

#### 1. **Naming Pattern**
All modules follow a consistent `{domain}-{suffix}` naming pattern:
- **Domain**: The functional area (e.g., `eveli`, `gamut`, `cockpit`, `contract`)
- **Suffix**: The module type (e.g., `api`, `composer`, `primitives`, `routes`, `intl`)
- **Examples**: `eveli-api`, `gamut-primitives`, `cockpit-composer`, `contract-api`

#### 2. **Module Architecture Hierarchy**

The monorepo has three types of modules:

##### **Core Development Modules** (`{domain}-{suffix}`)
- Individual functional modules for specific purposes
- Examples: `eveli-api`, `gamut-primitives`, `cockpit-composer`
- Location: `modules/{domain}-{suffix}/`

##### **Library Export Modules** (`lib-{domain}`)
- Aggregation modules that bundle all functionality from a domain for release
- Examples: `lib-eveli` → `@dxs-ts/eveli-ide`, `lib-gamut` → `@dxs-ts/gamut`
- Purpose: Release-ready packages for external consumption
- Contains comprehensive dependencies from their domain ecosystem

##### **Demo Applications** (`demo-app-{domain}`)
- Test/demonstration projects for library modules
- Examples: `demo-app-eveli` → `@dxs-ts/eveli-demo-app`
- Purpose: Testing and showcasing library functionality
- Contains comprehensive dependencies (ALL modules) for testing

#### 3. **File Structure**
Each module contains:
```
modules/{domain}-{suffix}/
├── package.json           # Module metadata and dependencies
├── tsconfig.json          # TypeScript config (extends generated config)
├── tsconfig.gen.json      # Auto-generated TypeScript configuration
├── index.ts               # Main entry point (optional initially)
└── {domain}-types.ts      # Type definitions
```

#### 4. **Module Declaration System**
Modules are declared in `package.json` dependency lists using the `@dxs-ts/` namespace:
```json
{
  "dependencies": {
    "@dxs-ts/eveli-api": "*",
    "@dxs-ts/gamut-primitives": "*"
  }
}
```

#### 5. **Domain Ecosystems**
- **Eveli Ecosystem**: All eveli-related modules, aggregated in `lib-eveli`
- **Gamut Ecosystem**: All gamut-related modules, aggregated in `lib-gamut`
- **Cross-domain**: Utilities used by multiple domains (added to multiple lib packages)

#### 6. **CRITICAL Import Rule**
**NEVER import a module from within itself using the @dxs-ts/ namespace.**

- ❌ **WRONG**: Inside `eveli-tree-api` directory, do NOT use `import { TreeNode } from '@dxs-ts/eveli-tree-api'`
- ✅ **CORRECT**: Inside `eveli-tree-api` directory, use relative imports: `import { TreeNode } from './tree-types'`
- ✅ **CORRECT**: Import `@dxs-ts/eveli-tree-api` ONLY from modules OUTSIDE the `eveli-tree-api` directory

**Rule**: Only import `@dxs-ts/{module-name}` from packages that are EXTERNAL to that module's directory. Within a module, always use relative imports for internal files.

## Step-by-Step Module Creation Process

### Step 1: Critical Questions the AI MUST Ask

Before creating any module, the AI must ask these questions:

#### **Domain Classification (MANDATORY)**
- **Question**: "Which domain is this module for?"
- **Options**:
  - Eveli domain only
  - Gamut domain only
  - Both domains (cross-domain utility)
  - New standalone domain (rare)

#### **Module Location (MANDATORY)**
- **Question**: "Where should this module be declared?"
- **Options**:
  - `/modules/` directory (most common - for standard domain modules)
  - `/` root directory (for high-level modules like `eveli-ide`, `gamut`, etc.)
- **Examples**:
  - Standard modules: `/modules/eveli-api/`, `/modules/gamut-primitives/`
  - High-level modules: `/eveli-ide/`, `/gamut/`

#### **Module Type**
- **Question**: "What type of module is this?"
- **Options**:
  - Core module (`{domain}-{suffix}`) - Standard development module
  - Library aggregator (`lib-{domain}`) - Release package
  - Demo application (`demo-app-{domain}`) - Testing application

#### **Module Purpose**
- **Question**: "What suffix best describes this module's purpose?"
- **Common suffixes**: `api`, `composer`, `primitives`, `routes`, `intl`, `form`, `theme`, `shell`

### Step 2: Navigate to Project Location

Based on the module location answer from Step 1:

**For modules in `/modules/` directory:**
```bash
cd /path/to/ts_setup/modules
```

**For high-level modules in root:**
```bash
cd /path/to/ts_setup/
```

### Step 3: Create Module Directory

```bash
mkdir {domain}-{suffix}
```

### Step 4: Create package.json

Template for package.json:
```json
{
  "name": "@dxs-ts/{domain}-{suffix}",
  "version": "${project.version}",
  "description": "{Domain} {suffix}",
  "main": "./index.ts",
  "type": "module",
  "dependencies": {},
  "peerDependencies": {
    "react": "*",
    "@tanstack/react-query": "*",
    "react-intl": "*"
  },
  "keywords": ["{domain}-{suffix}"],
  "private": true
}
```

### Step 5: Create tsconfig.json

```json
{
  "extends": "./tsconfig.gen.json"
}
```

### Step 6: Create Type Definitions

Create `{domain}-types.ts`:
```typescript
export interface {Domain}Api {
  id: string;
  // Add additional properties as needed
}
```

### Step 7: Add Module Dependencies (Based on Domain Answer)

**CRITICAL**: This step depends on the domain classification answer from Step 1.

#### **If Eveli domain only:**
Add to `modules/lib-eveli/package.json`:
```json
{
  "dependencies": {
    // ... existing dependencies
    "@dxs-ts/{domain}-{suffix}": "*"
  }
}
```

#### **If Gamut domain only:**
Add to `modules/lib-gamut/package.json`:
```json
{
  "dependencies": {
    // ... existing dependencies
    "@dxs-ts/{domain}-{suffix}": "*"
  }
}
```

#### **If both domains (cross-domain):**
Add to BOTH:
- `modules/lib-eveli/package.json`
- `modules/lib-gamut/package.json`

#### **If comprehensive access needed:**
Add to the main aggregator packages:
- `modules/demo-app-eveli/package.json`
- `modules/demo-app-gamut/package.json` (if applicable)

### Step 8: Generate Build Configuration

**When to run**: After creating all files and adding dependencies.

```bash
cd /path/to/ts_setup
pnpm build-modules
```

### Step 9: Verification

After running the build command, verify:
- [ ] Module appears in build output: `📦 Found module: @dxs-ts/{domain}-{suffix}`
- [ ] Path mappings generated: `→ @dxs-ts/{domain}-{suffix} at ../{domain}-{suffix}/index.ts`
- [ ] TSConfig generated: `✅ Generated tsconfig for @dxs-ts/{domain}-{suffix}`
- [ ] No build errors or missing dependencies

## When to Run pnpm build-modules

### **Always run after:**
1. Creating a new module
2. Adding module dependencies to package.json files
3. Modifying module structure

### **What it does:**
- Discovers all modules in the project
- Generates `tsconfig.gen.json` files with path mappings
- Updates the module registry
- Validates dependencies and detects circular references
- Creates TypeScript path mappings for imports

### **Expected output:**
- Module discovery: `📦 Found module: @dxs-ts/{name}`
- Dependency mapping: `📦 {consumer} → @dxs-ts/{name} at ../{folder}/index.ts`
- Config generation: `✅ Generated tsconfig for @dxs-ts/{name}`

## Usage After Creation

Once integrated, the module can be imported:
```typescript
import { DomainApi } from '@dxs-ts/{domain}-{suffix}';
```

## Key Integration Points

1. **Module Registry**: `.modules/registry.json` - Tracks all modules
2. **Generated Configs**: `tsconfig.gen.json` files - Auto-generated TypeScript configs
3. **Build System**: Vite-based build that processes all modules
4. **Dependency Graph**: Automatic analysis of module dependencies and usage

## Common Patterns

### **Creating API Module:**
- Domain: Ask user (eveli/gamut/both)
- Suffix: `api`
- Dependencies: Usually minimal, may depend on other APIs
- Add to corresponding `lib-{domain}` package

### **Creating Composer Module:**
- Domain: Ask user (eveli/gamut/both)
- Suffix: `composer`
- Dependencies: Usually depends on corresponding `{domain}-api` module
- Add to corresponding `lib-{domain}` package

### **Creating Cross-Domain Utility:**
- Domain: Both
- Suffix: Descriptive of function (`util`, `fetch`, `datetime`)
- Dependencies: Minimal
- Add to BOTH `lib-eveli` AND `lib-gamut` packages

## Error Prevention

### **Critical Mistakes to Avoid:**
1. **Forgetting to ask domain question** - Will result in module not being accessible
2. **Forgetting to ask module location question** - Module will be created in wrong directory
3. **Wrong dependency placement** - Module won't be importable in expected contexts
4. **Skipping build command** - TypeScript path mappings won't be generated
5. **Inconsistent naming** - Breaks the established pattern

### **Validation Checklist:**
- [ ] Asked domain classification question
- [ ] Asked module location question (modules/ vs root directory)
- [ ] Used correct naming pattern
- [ ] Created all required files
- [ ] Added dependencies to correct lib packages
- [ ] Ran build command successfully
- [ ] Verified module appears in build output