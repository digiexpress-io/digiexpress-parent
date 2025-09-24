import { execSync } from "node:child_process";



export interface RawCommit {
  hash: string;
  date: string;
  author: string;
  message: string;
  files: string[];
}


export declare namespace Command_MassiveCommitSweep {
  export interface Input {
    rootPath: string;
    terminationHash: string;
    commitsWithMessageToIgnore: string[];
  }
  export interface Result {
    rawCommits: RawCommit[];
  }
}

export class Command_MassiveCommitSweep {
  execute(input: Command_MassiveCommitSweep.Input): Command_MassiveCommitSweep.Result {
    const { rootPath, terminationHash, commitsWithMessageToIgnore } = input;
    
    console.log(`🚀 Starting massive commit sweep from ${terminationHash.substring(0, 7)}...`);
    
    try {
      // Get all commits since termination with file changes
      const gitCommand = `git log ${terminationHash}..HEAD --format="\n===COMMIT-START\n%H|%ad|%an|%s" --date=iso --name-only`;
      const output = execSync(gitCommand, {
        cwd: rootPath,
        encoding: 'utf8',
        timeout: 60000
      }).trim();
      
      if (!output) {
        console.log('ℹ️ No commits found since termination point');
        return { rawCommits: [] };
      }
      
      const rawCommits: RawCommit[] = [];
      const commitBlocks = output.split('===COMMIT-START').map(line => line.trim()).filter(e => !!e);
      
      for (const block of commitBlocks) {
        
        const lines = block.split('\n').map(line => line.trim()).filter(e => !!e);
        const meta = lines[0];


        // remove main log and filter out merge commits
        const files = lines.slice(1);
        const [hashPart, datePart, authorPart, messagePart] = meta.split('|');

        if(!messagePart) {
          // console.log(`⏭️ Ignoring commit: ${lines}`);
          continue;
        }

        // Apply ignore filters
        if (commitsWithMessageToIgnore.some(pattern => messagePart.includes(pattern))) {
          // console.log(`⏭️ Ignoring commit: ${messagePart}`);
          continue;
        }


        rawCommits.push({
          hash: hashPart.trim(),
          date: datePart.trim(),
          author: authorPart.trim(),
          message: messagePart.trim(),
          files: files.filter(f => f.trim())
        });
      }
      
      console.log(`✅ Collected ${rawCommits.length} commits for processing`);
      return { rawCommits };
      
    } catch (error) {
      throw new Error(`Failed massive commit sweep: ${error}`);
    }
  }
}