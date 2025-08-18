import { execSync } from "node:child_process";


export declare namespace Command_IdentifyTerminationCommit {
  export interface Input {
    rootPath: string;
    commitsCountAsFailsafe: number;
    commitsWithMessageAsPreviousRelease: string[];
  }
  export interface Result {
    terminationHash: string;
    commitCount: number;
    terminationType: 'primary' | 'fallback' | 'failsafe';
  }
}

export class Command_IdentifyTerminationCommit {
  execute(input: Command_IdentifyTerminationCommit.Input): Command_IdentifyTerminationCommit.Result {
    const { rootPath, commitsCountAsFailsafe, commitsWithMessageAsPreviousRelease } = input;
    
    console.log('🔍 Identifying termination commit...');
    
    try {
      // Get commit log with hash and message
      const gitCommand = `git log --format="%H|%s" -n ${commitsCountAsFailsafe}`;
      const output = execSync(gitCommand, {
        cwd: rootPath,
        encoding: 'utf8',
        timeout: 30000,

      }).trim();
      
      const commits = output.split('\n').map(line => {
        const [hash, message] = line.split('|');
        return { hash: hash.trim(), message: message.trim() };
      });
      
      // Primary: Look for "chore: preparing for next iteration"
      for (let i = 0; i < commits.length; i++) {
        if (commits[i].message === "chore: preparing for next iteration") {
          console.log(`✅ Found primary termination commit at position ${i + 1}`);
          return {
            terminationHash: commits[i].hash,
            commitCount: i,
            terminationType: 'primary'
          };
        }
      }
      
      // Secondary: Look for fallback messages
      for (let i = 0; i < commits.length; i++) {
        if (commitsWithMessageAsPreviousRelease.includes(commits[i].message)) {
          console.log(`✅ Found fallback termination commit at position ${i + 1}: "${commits[i].message}"`);
          return {
            terminationHash: commits[i].hash,
            commitCount: i,
            terminationType: 'fallback'
          };
        }
      }
      
      // Emergency: Hit the limit
      throw new Error(`💥 DIAGNOSTIC FAILURE: Could not find termination commit within ${commitsCountAsFailsafe} commits. Something has gone really wrong.`);
      
    } catch (error) {
      throw new Error(`Failed to identify termination commit: ${error}`);
    }
  }
}
