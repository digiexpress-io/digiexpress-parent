import { join } from "node:path";
import { execSync } from "node:child_process";
import { ModuleRegistry, ModuleCommits } from "../module-registry";

export declare namespace Command_ExtractGitHistory {
  export interface Input {
    rootPath: string;
    registry: ModuleRegistry;
  }

  export interface Result {
    moduleCommits: ModuleCommits;
  }
}

export class Command_ExtractGitHistory {
  execute(input: Command_ExtractGitHistory.Input): Command_ExtractGitHistory.Result {
    const { registry } = input;
    const modules = Object.values(registry.modules);
    
    console.log(`🔍 Extracting git history for ${modules.length} modules`);
    
    const moduleCommits: Command_ExtractGitHistory.Result['moduleCommits'] = [];
    
    for (const moduleInfo of modules) {
      const modulePath = join(input.rootPath, moduleInfo.path);
      
      console.log(`  📂 Processing ${moduleInfo.name} - ${modulePath} ...`);
      
      try {
        // Git command to get last 3 commits for the specific folder
        // Format: hash|date|author (pipe-separated for easy parsing)
        const gitCommand = `git log -n 3 --format="%H|%ad|%an" --date=iso -- "${moduleInfo.path}"`;
        
        const output = execSync(gitCommand, {
          cwd: input.rootPath,
          encoding: 'utf8',
          timeout: 10000 // 10 seconds timeout
        }).trim();
        
        const commits = output
          .split('\n')
          .filter(line => line.trim() !== '')
          .map(line => {
            const [hash, date, author] = line.split('|');
            return {
              hash: hash.trim(),
              date: date.trim(),
              author: author.trim()
            };
          });
        
        moduleCommits.push({
          moduleName: moduleInfo.name,
          commits
        });
        
        console.log(`    ✅ Found ${commits.length} commits for ${moduleInfo.name}`);
        
      } catch (error) {
        console.warn(`    ⚠️  Failed to get git history for ${moduleInfo.name}: ${error}`);
        
        // Add empty commits array for failed modules
        moduleCommits.push({
          moduleName: moduleInfo.name,
          commits: []
        });
      }
    }
    
    console.log(`✅ Git history extraction completed for ${moduleCommits.length} modules`);
    
    return { moduleCommits };
  }
}