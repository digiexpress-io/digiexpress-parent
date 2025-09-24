import { ModuleRegistry, ModuleRegistryCommitLogBuilder, ReleaseCommitLog } from "../module-registry";



export declare namespace Command_ExtractGitHistory {
  export interface Input {
    rootPath: string;
    registry: ModuleRegistry;
  }

  export interface Result {
    moduleLogs: ReleaseCommitLog[];
  }
}

export class Command_ExtractGitHistory {
  public static BACKEND_MODULE = 'backend';

  execute(input: Command_ExtractGitHistory.Input): Command_ExtractGitHistory.Result {
    const { registry } = input;
    
    const moduleLogs = new ModuleRegistryCommitLogBuilder()
      .setRegistry(registry)
      .setCommitsCountAsFailsafe(500)
      .setCommitsWithMessageToIgnore(['chore: update deps', 'docs:', 'gamut release', 'eveli ide release'])
      .setCommitsWithMessageAsPreviousRelease(['eveli ide release 0.0.443','release: v', 'bump version'])
      .setBackendModule(Command_ExtractGitHistory.BACKEND_MODULE, './mvn_setup')
      .setFrontendModule('./ts_setup')
      .setRootPath(input.rootPath)
      .build();

    return { moduleLogs };
  }
}