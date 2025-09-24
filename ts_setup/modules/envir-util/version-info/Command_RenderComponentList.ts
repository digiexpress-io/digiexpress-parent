import { ModuleCommits } from "./VersionInfoBuilder";

export declare namespace Command_RenderComponentList {
  export interface Input {
    title: string;
    components: string[];
    moduleCommits?: ModuleCommits;
  }
  export interface Result {
    success: boolean;
    renderedLines?: string[];
    error?: string;
  }
}



export class Command_RenderComponentList {
  execute(input: Command_RenderComponentList.Input): Command_RenderComponentList.Result {
    try {
      if (input.components.length === 0) {
        return { success: true, renderedLines: [] };
      }

      const renderedLines: string[] = [];

      // Add title
      renderedLines.push(`%c    ${input.title}:`);

      // Add empty line for spacing
      renderedLines.push('');

      // Add components
      input.components.forEach(component => {
        renderedLines.push(`%c      • ${component}`);

        const moduleCommits = (input.moduleCommits ?? []).find(target => target.moduleName === component)?.commits ?? [];

        for(const commit of moduleCommits) {
          renderedLines.push(`%c             - ${commit.hash.substring(0, 6)} by ${commit.author}, ${commit.date}`);
        }

      });

      return {
        success: true,
        renderedLines
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error rendering component list'
      };
    }
  }
}