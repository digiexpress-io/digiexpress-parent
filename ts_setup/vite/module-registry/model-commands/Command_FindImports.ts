import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, extname } from 'node:path';
import { ModuleInfo } from '../module-registry-types';

export declare namespace Command_FindImports {
  export interface Input {
    moduleInfos: ModuleInfo[];
    rootPath: string;
  }

  export interface Result {
    moduleInfos: ModuleInfo[];
  }
}

export class Command_FindImports {
  execute(input: Command_FindImports.Input): Command_FindImports.Result {
    const { moduleInfos, rootPath } = input;

    console.log(`🔍 Analyzing source code usage...`);

    // Create a copy to avoid mutating the input
    const updatedModuleInfos = moduleInfos.map(moduleInfo => {
      const actualUsage = _scanModuleSourceFiles(moduleInfo, rootPath);

      // Calculate actual dependencies by category
      const actualExternalDependencies = actualUsage.filter(dep => !dep.startsWith('@dxs-ts/'));
      const actualInternalDependencies = actualUsage.filter(dep => dep.startsWith('@dxs-ts/'));

      // Compare declared vs actual usage to find discrepancies
      const declaredDeps = new Set(moduleInfo.dependencies);
      const actualDeps = new Set(actualUsage);
      const missingDeps = [...actualDeps].filter(dep => !declaredDeps.has(dep));
      const unusedDeps = [...declaredDeps].filter(dep => !actualDeps.has(dep));

      if (missingDeps.length > 0) {
        console.warn(`   ⚠️  ${moduleInfo.name}: Missing dependencies: ${missingDeps.join(', ')}`);
      }
      if (unusedDeps.length > 0) {
        console.warn(`   ⚠️  ${moduleInfo.name}: Unused dependencies: ${unusedDeps.join(', ')}`);
      }

      console.log(`   ✅ ${moduleInfo.name}: ${actualUsage.length} dependencies used, ${missingDeps.length} missing, ${unusedDeps.length} unused`);

      return {
        ...moduleInfo,
        actualDependencies: actualUsage,
        actualExternalDependencies,
        actualInternalDependencies,
        missingDependencies: missingDeps,
        unusedDependencies: unusedDeps
      };
    });

    return { moduleInfos: updatedModuleInfos };
  }


}


function _scanModuleSourceFiles(moduleInfo: ModuleInfo, rootPath: string): string[] {
  const modulePath = join(rootPath, moduleInfo.path);
  const usedDependencies = new Set<string>();

  _scanDirectory(modulePath, (filePath) => {
    const dependencies = _extractImportsFromFile(filePath);
    dependencies
      .filter(dep => moduleInfo.name !== dep)
      .forEach(dep => usedDependencies.add(dep));
  });

  return Array.from(usedDependencies);
}

// Pure transformative functions
function _scanDirectory(dirPath: string, onFileFound: (filePath: string) => void): void {
  const entries = readdirSync(dirPath);

  for (const entry of entries) {
    const fullPath = join(dirPath, entry);
    const stat = statSync(fullPath);

    if (stat.isDirectory()) {
      // Skip node_modules and dist directories
      if (!['node_modules', 'dist', '.git'].includes(entry)) {
        _scanDirectory(fullPath, onFileFound);
      }
    } else if (stat.isFile()) {
      // Process TypeScript files
      const ext = extname(entry);
      if (['.ts', '.tsx', '.js', '.jsx'].includes(ext)) {
        onFileFound(fullPath);
      }
    }
  }
}

function _extractImportsFromFile(filePath: string): string[] {
  try {
    const content = readFileSync(filePath, 'utf-8');
    const dependencies = new Set<string>();

    // Match import statements: import { x } from 'package'
    const importMatches = content.match(/import\s+.*?\s+from\s+['"]([^'"]+)['"]/g) || [];

    // Match export statements: export * from 'package'  
    const exportMatches = content.match(/export\s+.*?\s+from\s+['"]([^'"]+)['"]/g) || [];

    // Match dynamic imports: import('package')
    const dynamicMatches = content.match(/import\s*\(\s*['"]([^'"]+)['"]\s*\)/g) || [];

    // Process all matches
    const allMatches = [...importMatches, ...exportMatches, ...dynamicMatches];

    for (const match of allMatches) {
      const packageMatch = match.match(/['"]([^'"]+)['"]/);
      if (packageMatch) {
        const packageName = packageMatch[1];

        // Only track non-relative imports (external and internal dependencies)
        if (!packageName.startsWith('./') && !packageName.startsWith('../')) {
          // Normalize subpath imports to their base package
          const normalizedPackage = _normalizePackageName(packageName);
          dependencies.add(normalizedPackage);
        }
      }
    }

    return Array.from(dependencies);
  } catch (error) {
    console.warn(`Could not scan imports in ${filePath}:`, error);
    return [];
  }
}

function _normalizePackageName(packageName: string): string {
  // Don't normalize internal @dxs-ts packages
  if (packageName.startsWith('@dxs-ts/')) {
    return packageName;
  }

  // Handle scoped packages (@org/package/subpath)
  if (packageName.startsWith('@')) {
    const parts = packageName.split('/');
    if (parts.length >= 2) {
      // Return @org/package (first two parts)
      return `${parts[0]}/${parts[1]}`;
    }
    return packageName;
  }

  // Handle regular packages (package/subpath)
  const parts = packageName.split('/');
  if (parts.length > 1) {
    // Return just the package name (first part)
    return parts[0];
  }

  // Already a simple package name
  return packageName;
}