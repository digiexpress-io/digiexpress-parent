import { join } from "node:path";
import { ModuleInfo, ModuleRegistry } from "../module-registry";
import { existsSync, readFileSync } from "node:fs";
import { BuildConfig } from '../module-registry';

export declare namespace Command_UpdatePeerDependencies {
  export interface Input {
    rootPath: string;
    registry: ModuleRegistry;
    moduleInfo: ModuleInfo;
    packageJson: any;
  }

  export interface Result {
    trace: BuildConfig;
    addedPeerDependencies: Record<string, string>;
    updatedPeerDependencies: Record<string, { from: string; to: string }>;
  }
}

export class Command_UpdatePeerDependencies {
  execute(input: Command_UpdatePeerDependencies.Input): Command_UpdatePeerDependencies.Result {
    const { registry, moduleInfo, packageJson } = input;


    if (!moduleInfo) {
      throw new Error(`Module not found in registry`);
    }

    console.log(`🔄 Updating peer dependencies for ${moduleInfo.name}`);

    // Ensure peerDependencies object exists
    if (!packageJson.peerDependencies) {
      packageJson.peerDependencies = {};
    }


    const modulePath = join(input.rootPath, moduleInfo.path);
    const tracePath = join(modulePath, 'dist', 'trace.json');

    console.log(`🔍 Reading trace.json for ${moduleInfo.name}`);

    // Read and validate trace.json
    if (!existsSync(tracePath)) {
      throw new Error(`trace.json not found: ${tracePath}`);
    }

    let traceData: BuildConfig;
    try {
      const traceContent = readFileSync(tracePath, 'utf-8');
      traceData = JSON.parse(traceContent);

      // Validate trace structure
      if (!traceData.metadata?.externalDependencies) {
        throw new Error('Invalid trace.json: missing metadata.externalDependencies');
      }

      console.log(`✅ Trace loaded: ${Object.keys(traceData.metadata.externalDependencies).length} external dependencies`);

    } catch (error: any) {
      throw new Error(`Failed to parse trace.json: ${error.message}`);
    }



    const currentPeerDeps = { ...packageJson.peerDependencies };
    const tracePeerDeps = traceData.metadata.externalDependencies;

    const addedPeerDependencies: Record<string, string> = {};
    const updatedPeerDependencies: Record<string, { from: string; to: string }> = {};
    const removedPeerDependencies: string[] = [];

    console.log(`🔄 Updating peer dependencies from trace data`);

    // Update peer dependencies from trace
    for (const [depName, version] of Object.entries(tracePeerDeps)) {
      if (currentPeerDeps[depName]) {
        // Check if version changed
        if (currentPeerDeps[depName] !== version) {
          updatedPeerDependencies[depName] = {
            from: currentPeerDeps[depName],
            to: version
          };
          packageJson.peerDependencies[depName] = version.startsWith('^') ? version : `^${version}`;;
          console.log(`   🔄 Updated: ${depName} (${currentPeerDeps[depName]} → ${version})`);
        }
      } else {
        // New peer dependency
        addedPeerDependencies[depName] = version.startsWith('^') ? version : `^${version}`;
        packageJson.peerDependencies[depName] = version;
        console.log(`   ➕ Added: ${depName}@${version}`);
      }
    }

    // Find removed peer dependencies (exist in package.json but not in trace)
    for (const [depName, version] of Object.entries(currentPeerDeps)) {
      if (version === '*') {
        removedPeerDependencies.push(depName);
        delete packageJson.peerDependencies[depName];
        console.log(`   🗑️  Removed: ${depName}`);
      }
    }

    // Clean up empty peerDependencies object
    if (Object.keys(packageJson.peerDependencies).length === 0) {
      delete packageJson.peerDependencies;
    }

    const totalChanges = Object.keys(addedPeerDependencies).length +
      Object.keys(updatedPeerDependencies).length +
      removedPeerDependencies.length;

    console.log(`✅ Peer dependencies updated (${totalChanges} changes)`);

    return {
      addedPeerDependencies,
      updatedPeerDependencies,
      trace: traceData
    };
  }
}