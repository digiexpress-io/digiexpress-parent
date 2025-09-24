import { execSync } from "node:child_process";

export declare namespace Command_RunPnpmCommand {
 export interface Input {
   command: string;
   args?: string[];
   cwd?: string;
   timeout?: number;
 }
 
 export interface Result {
   success: boolean;
   exitCode: number;
   stdout: string;
   stderr: string;
   command: string;
   duration: number;
 }
}

export class Command_RunPnpmCommand {
 execute(input: Command_RunPnpmCommand.Input): Command_RunPnpmCommand.Result {
   const { command, args = [], cwd = process.cwd(), timeout = 30000 } = input;
   
   const fullCommand = `pnpm ${command} ${args.join(' ')}`.trim();
   const startTime = Date.now();
   
   console.log(`🏃 Running: ${fullCommand}`);
   if (cwd !== process.cwd()) {
     console.log(`   📁 Working directory: ${cwd}`);
   }
   
   try {
     const result = execSync(fullCommand, {
       cwd,
       encoding: 'utf-8',
       timeout,
       stdio: 'pipe' // Capture both stdout and stderr
     });
     
     const duration = Date.now() - startTime;
     
     console.log(`✅ Command succeeded (${duration}ms)`);
     
     return {
       success: true,
       exitCode: 0,
       stdout: result.toString(),
       stderr: '',
       command: fullCommand,
       duration
     };
     
   } catch (error: any) {
     const duration = Date.now() - startTime;
     
     console.error(`❌ Command failed (${duration}ms)`);
     console.error(`   Exit code: ${error.status || 'unknown'}`);
     
     if (error.stdout) {
       console.log(`   📤 stdout: ${error.stdout.toString()}`);
     }
     
     if (error.stderr) {
       console.error(`   📥 stderr: ${error.stderr.toString()}...`);
     }
     
     return {
       success: false,
       exitCode: error.status || -1,
       stdout: error.stdout?.toString() || '',
       stderr: error.stderr?.toString() || error.message,
       command: fullCommand,
       duration
     };
   }
 }
}