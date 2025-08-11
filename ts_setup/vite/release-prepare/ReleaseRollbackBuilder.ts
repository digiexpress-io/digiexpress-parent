import { ModuleRegistry } from "../module-registry";
import { Command_RestorePackageJson } from "./Command_RestorePackageJson";

export interface ReleaseRollbackResult {
 moduleName: string;
 restoredPath: string;
 backupPath: string;
 backupDeleted: boolean;
}

export class ReleaseRollbackBuilder {
 build(registry: ModuleRegistry, moduleName: string): ReleaseRollbackResult {
   console.log(`♻️  Restoring module from publish prep: ${moduleName}`);

   try {
     const restoreCmd = new Command_RestorePackageJson();
     
     const result = restoreCmd.execute({
       registry,
       moduleName
     });

     console.log(`✅ Module restoration completed for ${moduleName}`);
     
     return {
       moduleName,
       restoredPath: result.restoredPath,
       backupPath: result.backupPath,
       backupDeleted: result.backupDeleted
     };

   } catch (error) {
     console.error(`❌ Failed to restore module ${moduleName}:`, error);
     throw error;
   }
 }
}