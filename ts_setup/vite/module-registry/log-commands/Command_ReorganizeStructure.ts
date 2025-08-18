import { ModuleLog, ReleaseCommitLog } from "../module-registry-types";

export declare namespace Command_ReorganizeStructure {
  export interface Input {
    moduleLogs: ModuleLog[];
  }
  export interface Result {
    logs: ReleaseCommitLog[];
  }
}

export class Command_ReorganizeStructure {
  execute(input: Command_ReorganizeStructure.Input): Command_ReorganizeStructure.Result {
    const { moduleLogs } = input;
    
    console.log(`🔄 Reorganizing ${moduleLogs.length} module logs...`);
    
    // Step 1: Collect all commits with their module names
    const commitMap = new Map<string, ReleaseCommitLog>();
    
    for (const moduleLog of moduleLogs) {
      for (const commit of moduleLog.logs) {
        const existingCommit = commitMap.get(commit.hash);
        
        if (existingCommit) {
          // Step 2: Combine same hashes - add module to existing commit
          if (!existingCommit.modules.includes(moduleLog.moduleName)) {
            existingCommit.modules.push(moduleLog.moduleName);
          }
        } else {
          // New commit - create entry with module name
          commitMap.set(commit.hash, {
            hash: commit.hash,
            date: commit.date,
            author: commit.author,
            comment: commit.comment,
            issueId: commit.issueId,
            modules: [moduleLog.moduleName]  // Step 3: Replace files with module names
          });
        }
      }
    }
    
    // Step 1: Sort by date (newest first)
    const sortedCommits = Array.from(commitMap.values()).sort((a, b) => {
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateB.getTime() - dateA.getTime();
    });
    
    // Sort modules within each commit for consistency
    sortedCommits.forEach(commit => {
      commit.modules.sort();
    });
    
    
    // Show modules affected by most commits
    const moduleCommitCounts = new Map<string, number>();
    sortedCommits.forEach(commit => {
      commit.modules.forEach(module => {
        moduleCommitCounts.set(module, (moduleCommitCounts.get(module) || 0) + 1);
      });
    });
 
    return { logs: sortedCommits };
  }
}