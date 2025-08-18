import { RawCommit } from "./Command_MassiveCommitSweep";
import { ModuleCommitLog, ModuleLog, ModuleRegistry } from "../module-registry-types";
import { join } from "node:path";

export declare namespace Command_SpreadIntoModules {
  export interface Input {
    rawCommits: RawCommit[];
    registry: ModuleRegistry;
    fontendModule: { path: string };
    backendModule: { name: string; path: string };
  }
  export interface Result {
    moduleLogs: ModuleLog[];
  }
}

export class Command_SpreadIntoModules {
  execute(input: Command_SpreadIntoModules.Input): Command_SpreadIntoModules.Result {
    const { rawCommits, registry, backendModule } = input;
    
    console.log(`📊 Spreading ${rawCommits.length} commits into modules...`);
    const moduleCommitMap: Record<string, ModuleCommitLog[]> = {};
    
    // Initialize all modules
    Object.keys(registry.modules).forEach(moduleName => {
      moduleCommitMap[moduleName] = [];
    });
    moduleCommitMap[backendModule.name] = [];
    
    for (const commit of rawCommits) {
      const commitLog: ModuleCommitLog = {
        hash: commit.hash,
        date: commit.date,
        author: commit.author,
        comment: commit.message,
        issueId: this.extractIssueId(commit.message),
        files: commit.files
      };
      
      let assignedToModule = false;
      
      // Check TypeScript modules
      for (const [moduleName, moduleInfo] of Object.entries(registry.modules)) {

        if (commit.files.some(file => file.startsWith(join(input.fontendModule.path, moduleInfo.path)))) {
          moduleCommitMap[moduleName].push(commitLog);
          assignedToModule = true;
        }
      }

      // Check backend module
      if (commit.files.some(file => file.startsWith(join(backendModule.path)))) {
        moduleCommitMap[backendModule.name].push(commitLog);
        assignedToModule = true;
      }
      
      if (!assignedToModule) {
        //console.log(`⚠️ Orphaned commit: ${commit.hash.substring(0, 7)} - ${commit.message}`);
      }
    }
    
    // Convert to ModuleLog array
    const moduleLogs: ModuleLog[] = Object.entries(moduleCommitMap)
      .filter(([_, logs]) => logs.length > 0)
      .map(([moduleName, logs]) => ({
        moduleName,
        logs: logs.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
      }));
    
    console.log(`✅ Distributed commits across ${moduleLogs.length} modules`);
    return { moduleLogs };
  }
  
  private extractIssueId(message: string): string {
    // Extract issue ID from commit message (common patterns)
    const patterns = [
      /\[([A-Z]+-\d+)\]/,      // [PROJ-123]
      /#(\d+)/,                // #123
      /\b([A-Z]+-\d+)\b/,      // PROJ-123
    ];
    
    for (const pattern of patterns) {
      const match = message.match(pattern);
      if (match) return match[1];
    }
    
    return '';
  }
}