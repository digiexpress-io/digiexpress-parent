import { load } from "js-yaml";
import { execSync } from "node:child_process";
import { existsSync, readFileSync } from "node:fs";

export declare namespace Command_ResolvePackageVersions {
  export interface Input {
    external: string[];
    global: string[];
    lockfilePath?: string;
  }

  export interface Result {
    resolvedVersions: Record<string, string>;
    unresolvedPackages: string[];
  }
}


export class Command_ResolvePackageVersions {
  execute(input: Command_ResolvePackageVersions.Input): Command_ResolvePackageVersions.Result {
    const { lockfilePath = 'pnpm-lock.yaml' } = input;

    // Combine external and global package names
    const allPackages = _resolvedExternalDeps(input);

    console.log(`🔍 Resolving versions for ${allPackages.length} packages`);

    const resolvedVersions: Record<string, string> = {};
    const unresolvedPackages: string[] = [];

    try {
      // Try to get versions from pnpm lockfile first
      const lockVersions = _parsePnpmLockfile(lockfilePath);
      for (const packageName of allPackages) {

        const version = _findLockVersion(packageName, lockVersions);

        if (version) {
          resolvedVersions[packageName] = version;
          console.log(`   ✅ ${packageName}: ${version}`);
        } else {
          // Fallback: try to get version via pnpm list
          const fallbackVersion = _getVersionViaPnpmList(packageName);

          if (fallbackVersion) {
            resolvedVersions[packageName] = fallbackVersion;
            console.log(`   ✅ ${packageName}: ${fallbackVersion} (via pnpm list)`);
          } else {
            unresolvedPackages.push(packageName);
            console.warn(`   ❌ ${packageName}: version not found`);
          }
        }
      }

      console.log(`✅ Resolved ${Object.keys(resolvedVersions).length}/${allPackages.length} package versions`);

      if (unresolvedPackages.length > 0) {
        console.warn(`⚠️  Could not resolve versions for: ${unresolvedPackages.join(', ')}`);
      }

      return {
        resolvedVersions,
        unresolvedPackages
      };

    } catch (error) {
      console.log(allPackages)
      console.error('❌ Failed to resolve package versions:', error);
      throw error;
    }
  }
}

// Pure transformative functions
function _parsePnpmLockfile(lockfilePath: string): Record<string, string> {
  try {
    if (!existsSync(lockfilePath)) {
      console.warn(`Lockfile not found: ${lockfilePath}`);
      return {};
    }

    const lockContent = readFileSync(lockfilePath, 'utf-8');
    const lockData = load(lockContent) as any;

    const versions: Record<string, string> = {};
    
    // Navigate to root importer dependencies
    const rootImporter = lockData.importers?.['.'];
    if (!rootImporter) {
      console.warn('No root importer found in lockfile');
      return {};
    }

    // Parse dependencies from lockfile
    if (rootImporter.dependencies) {
      for (const [name, info] of Object.entries(rootImporter.dependencies)) {
        if (typeof info === 'object' && (info as any).version) {
          versions[name] = (info as any).version;
        } else if (typeof info === 'string') {
          // Handle simple string format like "1.0.0"
          versions[name] = info as string;
        }
      }
    }

    // Also check devDependencies if present
    if (rootImporter.devDependencies) {
      for (const [name, info] of Object.entries(rootImporter.devDependencies)) {
        if (!versions[name]) { // Don't overwrite regular deps
          if (typeof info === 'object' && (info as any).version) {
            versions[name] = (info as any).version;
          } else if (typeof info === 'string') {
            versions[name] = info as string;
          }
        }
      }
    }

    return versions;

  } catch (error) {
    console.warn(`Failed to parse lockfile ${lockfilePath}:`, error);
    return {};
  }
}

function _getVersionViaPnpmList(packageName: string): string | null {
  try {
    const output = execSync(`pnpm list ${packageName} --json --depth=0`, {
      encoding: 'utf-8',
      stdio: 'pipe' // Suppress stderr
    });

    const data = JSON.parse(output);
    return data.dependencies?.[packageName]?.version || null;

  } catch (error) {
    // Package not found or other error
    return null;
  }
}

function _resolvedExternalDeps(input: Command_ResolvePackageVersions.Input): string[] {
  return Array.from(new Set<string>([...input.external, ...input.global]))
    .filter(value => !value.startsWith("."))
    .filter(value => !value.startsWith("@dxs-ts"))
    .filter(value => !value.startsWith("/"))
    .filter(value => !value.startsWith("@mui/icons-material/"))
    .filter(value => !value.startsWith("@mui/material/"))
    .filter(value => !value.startsWith("@mui/utils/"))
    .filter(value => !value.startsWith("react-date-picker/"))

    .filter(value => !value.startsWith("react/jsx-runtime"))
    .filter(value => !value.endsWith(".css"))
    .sort()
}

// Pure function to clean version strings
function _cleanVersionString(versionString: string): string {
  // Remove everything after the first opening parenthesis
  // '11.0.0(@types/react-dom@18.3.7(...))' -> '11.0.0'
  const match = versionString.match(/^([^(]+)/);
  return match ? match[1].trim() : versionString;
}

function _explodePathVariants(input: string): string[] {
  // Split the path by forward slashes
  const parts = input.split('/');
  const variants: string[] = [];
  
  // Build progressive variants
  for (let i = 1; i <= parts.length; i++) {
    variants.push(parts.slice(0, i).join('/'));
  }
  
  return variants.sort((a, b) => b.length - a.length);
}


function _findLockVersion(packageName: string, lockFile: Record<string, string>): string {
  const version = lockFile[packageName] ?? _explodePathVariants(packageName).map(exp => lockFile[exp]).find(found => !!found);
  return _cleanVersionString(version);
}