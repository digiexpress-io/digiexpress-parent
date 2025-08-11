import { Command_RenderComponentList } from "./Command_RenderComponentList";
import { Command_RenderLogo } from "./Command_RenderLogo";
import { Command_RenderProjectInfo } from "./Command_RenderProjectInfo";
import { Command_RenderSeparator } from "./Command_RenderSeparator";
import { LOGOS, THEMES } from "./themes";

export type ModuleCommits = Array<{
  moduleName: string;
  commits: Array<{ 
    date: string; 
    hash: string; 
    author: string; 
  }>;
}>;

// ASCII Art Builder
export class VersionInfoBuilder {

  private selectedLogo?: string;
  private theme: 'purple' | 'red' = 'purple';
  private projectName?: string;
  private version?: string;
  private moduleCommits?: ModuleCommits;
  private releaseDate?: string | Date;
  private internalComponents: string[] = [];
  private externalComponents: string[] = [];

  setLogo(logoKey: string): this {
    if (!LOGOS[logoKey]) {
      throw new Error(`Logo '${logoKey}' not found`);
    }
    this.selectedLogo = logoKey;
    return this;
  }

  setCommits(moduleCommits: ModuleCommits): this {
    this.moduleCommits = moduleCommits;
    return this;
  }

  setTheme(theme: 'purple' | 'red'): this {
    this.theme = theme;
    return this;
  }

  setProjectInfo(projectName: string, version: string, releaseDate: string | Date): this {
    this.projectName = projectName;
    this.version = version;
    this.releaseDate = releaseDate;
    return this;
  }

  addInternalComponents(components: string[]): this {
    this.internalComponents = [...this.internalComponents, ...components];
    return this;
  }

  addExternalComponents(components: string[]): this {
    this.externalComponents = [...this.externalComponents, ...components];
    return this;
  }

  render(): void {
    if (!this.selectedLogo || !this.projectName || !this.version || !this.releaseDate) {
      throw new Error('Missing required fields: logo, projectName, version, and releaseDate must be set');
    }

    const theme = THEMES[this.theme];
    
    // Calculate separator length
    const maxLength = Math.max(
      this.projectName.length,
      `v${this.version} • ${this.releaseDate}`.length,
      40
    );
    const separatorLength = Math.max(maxLength + 8, 40);

    // Execute commands in sequence
    const logoCommand = new Command_RenderLogo();
    const separatorCommand = new Command_RenderSeparator();
    const projectInfoCommand = new Command_RenderProjectInfo();
    const componentListCommand = new Command_RenderComponentList();

    // Render logo
    const logoResult = logoCommand.execute({ logo: LOGOS[this.selectedLogo] });
    if (!logoResult.success) {
      console.error('Failed to render logo:', logoResult.error);
      return;
    }

    // Render separator
    const separatorResult = separatorCommand.execute({ length: separatorLength });
    if (!separatorResult.success) {
      console.error('Failed to render separator:', separatorResult.error);
      return;
    }

    // Render project info
    const projectResult = projectInfoCommand.execute({
      projectName: this.projectName,
      version: this.version,
      releaseDate: this.releaseDate,
    });
    if (!projectResult.success) {
      console.error('Failed to render project info:', projectResult.error);
      return;
    }

    // Render internal components
    const internalResult = componentListCommand.execute({
      title: 'Internal Components',
      components: this.internalComponents,
      moduleCommits: this.moduleCommits
    });
    if (!internalResult.success) {
      console.error('Failed to render internal components:', internalResult.error);
      return;
    }

    // Render external components
    const externalResult = componentListCommand.execute({
      title: 'External Components',
      components: this.externalComponents
    });
    if (!externalResult.success) {
      console.error('Failed to render external components:', externalResult.error);
      return;
    }

    // Output everything to console
    const allStyles: string[] = [];
    const allLines: string[] = [];

    // Add logo
    logoResult.renderedLines?.forEach(line => {
      allLines.push(line);
      allStyles.push(theme.logo);
    });

    // Add empty line
    allLines.push('');

    // Add separator
    if (separatorResult.renderedLine) {
      allLines.push(separatorResult.renderedLine);
      allStyles.push(theme.separator);
    }

    // Add project info
    projectResult.renderedLines?.forEach((line, index) => {
      allLines.push(line);
      allStyles.push(index === 0 ? theme.projectName : theme.info);
    });

    // Add separator
    if (separatorResult.renderedLine) {
      allLines.push(separatorResult.renderedLine);
      allStyles.push(theme.separator);
    }

    // Add internal components
    if (internalResult.renderedLines && internalResult.renderedLines.length > 0) {
      allLines.push('');
      internalResult.renderedLines.forEach((line, index) => {
        allLines.push(line);
        if (line.trim() === '') {
          // Empty line, no style needed
        } else if (line.includes(':')) {
          allStyles.push(theme.componentTitle);
          
        } else {
          allStyles.push(theme.componentItem);
        }
      });
    }

    // Add external components
    if (externalResult.renderedLines && externalResult.renderedLines.length > 0) {
      allLines.push('');
      externalResult.renderedLines.forEach((line, index) => {
        allLines.push(line);
        if (line.trim() === '') {
          // Empty line, no style needed
        } else if (line.includes(':')) {
          allStyles.push(theme.componentTitle);
        } else {
          allStyles.push(theme.componentItem);
        }
      });
    }

    // Final separator
    allLines.push('');
    if (separatorResult.renderedLine) {
      allLines.push(separatorResult.renderedLine);
      allStyles.push(theme.separator);
    }

    // Execute the console.log with all formatting
    const finalOutput = allLines.join('\n').replace(/ /g, '\u00A0');
    console.log(finalOutput, ...allStyles);
  }
}