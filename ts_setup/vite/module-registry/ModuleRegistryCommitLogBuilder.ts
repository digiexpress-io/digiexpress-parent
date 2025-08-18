import { ModuleLog, ModuleRegistry, ReleaseCommitLog } from "../module-registry";
import { Command_IdentifyTerminationCommit } from "./log-commands/Command_IdentifyTerminationCommit";
import { Command_MassiveCommitSweep } from "./log-commands/Command_MassiveCommitSweep";
import { Command_ReorganizeStructure } from "./log-commands/Command_ReorganizeStructure";
import { Command_SpreadIntoModules } from "./log-commands/Command_SpreadIntoModules";


export class ModuleRegistryCommitLogBuilder {
 private registry?: ModuleRegistry;
  private commitsCountAsFailsafe?: number;
  private commitsWithMessageToIgnore?: string[];
  private commitsWithMessageAsPreviousRelease?: string[];
  private backendModule?: { name: string; path: string };
  private frontendModule?: { path: string };
  private rootPath?: string;

  setRegistry(registry: ModuleRegistry): this {
    this.registry = registry;
    return this;
  }

  setCommitsCountAsFailsafe(count: number): this {
    this.commitsCountAsFailsafe = count;
    return this;
  }

  setCommitsWithMessageToIgnore(messages: string[]): this {
    this.commitsWithMessageToIgnore = messages;
    return this;
  }

  setCommitsWithMessageAsPreviousRelease(messages: string[]): this {
    this.commitsWithMessageAsPreviousRelease = messages;
    return this;
  }

  setBackendModule(name: string, path: string): this {
    this.backendModule = { name, path };
    return this;
  }

  setFrontendModule(path: string): this {
    this.frontendModule = { path };
    return this;
  }

  setRootPath(path: string): this {
    this.rootPath = path;
    return this;
  }

  build(): ReleaseCommitLog[] {
    // Validate all required fields
    if (!this.registry) {
      throw new Error('Registry is required. Call setRegistry() first.');
    }
    if (this.commitsCountAsFailsafe === undefined) {
      throw new Error('Commits count failsafe is required. Call setCommitsCountAsFailsafe() first.');
    }
    if (!this.commitsWithMessageToIgnore) {
      throw new Error('Commits with message to ignore is required. Call setCommitsWithMessageToIgnore() first.');
    }
    if (!this.commitsWithMessageAsPreviousRelease) {
      throw new Error('Commits with message as previous release is required. Call setCommitsWithMessageAsPreviousRelease() first.');
    }
    if (!this.backendModule) {
      throw new Error('Backend module is required. Call setBackendModule() first.');
    }
    if (!this.frontendModule) {
      throw new Error('Frontend module is required. Call setFrontendModule() first.');
    }
    if (!this.rootPath) {
      throw new Error('Root path is required. Call setRootPath() first.');
    }

    console.log('🏗️ Starting ModuleRegistryCommitLogBuilder pipeline...');

    // Command 1: Identify termination commit
    const terminationCommand = new Command_IdentifyTerminationCommit();
    const terminationResult = terminationCommand.execute({
      rootPath: this.rootPath,
      commitsCountAsFailsafe: this.commitsCountAsFailsafe,
      commitsWithMessageAsPreviousRelease: this.commitsWithMessageAsPreviousRelease
    });

    console.log(`📍 Termination point: ${terminationResult.terminationHash.substring(0, 7)} (${terminationResult.terminationType})`);

    // Command 2: Massive commit sweep
    const sweepCommand = new Command_MassiveCommitSweep();
    const sweepResult = sweepCommand.execute({
      rootPath: this.rootPath,
      terminationHash: terminationResult.terminationHash,
      commitsWithMessageToIgnore: this.commitsWithMessageToIgnore
    });

    // Command 3: Spread into modules
    const spreadCommand = new Command_SpreadIntoModules();
    const spreadResult = spreadCommand.execute({
      rawCommits: sweepResult.rawCommits,
      registry: this.registry,
      backendModule: this.backendModule,
      fontendModule: this.frontendModule
    });


    const reorgCommand = new Command_ReorganizeStructure();
    const {logs} = reorgCommand.execute({
      moduleLogs: spreadResult.moduleLogs
    });

    console.log('🎉 ModuleRegistryCommitLogBuilder pipeline completed successfully!');
    return logs;
  }
}