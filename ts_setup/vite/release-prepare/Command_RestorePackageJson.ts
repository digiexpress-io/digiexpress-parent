import { join } from "node:path";
import { existsSync, readFileSync, unlinkSync, writeFileSync } from "node:fs";

import { ModuleRegistry } from "../module-registry";

export declare namespace Command_RestorePackageJson {
  export interface Input {
    rootPath: string;
    registry: ModuleRegistry;
    moduleName: string;
  }

  export interface Result {
    restoredPath: string;
    backupPath: string;
    backupDeleted: boolean;
  }
}

export class Command_RestorePackageJson {
  execute(input: Command_RestorePackageJson.Input): Command_RestorePackageJson.Result {
    const { registry, moduleName } = input;

    const moduleInfo = registry.modules[moduleName];
    if (!moduleInfo) {
      throw new Error(`Module '${moduleName}' not found in registry`);
    }

    const modulePath = join(input.rootPath, moduleInfo.path);
    const originalPath = join(modulePath, 'package.json');
    const backupPath = join(modulePath, 'package.json.versionsbackup');

    console.log(`🔄 Restoring package.json for ${moduleName}`);

    // Check if backup exists
    if (!existsSync(backupPath)) {
      throw new Error(`Backup file not found: ${backupPath}`);
    }

    // Restore from backup (overwrite current package.json)
    const backupContent = readFileSync(backupPath, 'utf-8');
    writeFileSync(originalPath, backupContent, 'utf-8');
    console.log(`✅ Restored package.json from backup`);

    // Delete the backup file
    unlinkSync(backupPath);
    console.log(`🗑️  Deleted backup file: ${backupPath}`);

    return {
      restoredPath: originalPath,
      backupPath,
      backupDeleted: true
    };
  }
}