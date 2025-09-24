import { execSync } from "node:child_process";

export declare namespace Command_GitCommit {
  export interface Input {
    message: string;
    cwd?: string;
  }
  
  export interface Result {
    success: boolean;
    commitHash?: string;
    error?: string;
  }
}

export class Command_GitCommit {
  execute(input: Command_GitCommit.Input): Command_GitCommit.Result {
    const { message, cwd = process.cwd() } = input;
    
    console.log(`📝 Creating git commit: "${message}"`);
    
    try {
      // Add all changes
      execSync('git add .', { cwd, stdio: 'pipe' });
      
      // Commit
      const result = execSync(`git commit -m "${message}"`, { 
        cwd, 
        encoding: 'utf-8',
        stdio: 'pipe'
      });
      
      // Get commit hash
      const commitHash = execSync('git rev-parse HEAD', { 
        cwd, 
        encoding: 'utf-8',
        stdio: 'pipe'
      }).trim();
      
      console.log(`✅ Commit created: ${commitHash.substring(0, 8)}`);
      
      return { success: true, commitHash };
      
    } catch (error: any) {
      console.error(`❌ Git commit failed: ${error.message}`);
      return { 
        success: false, 
        error: error.message 
      };
    }
  }
}