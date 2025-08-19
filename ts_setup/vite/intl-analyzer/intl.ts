import { ModuleRegistry, ModuleInfo } from '../module-registry';
import { Command_AnalyzeModuleUsage } from './Command_AnalyzeModuleUsage';
import { Command_DiscoverLanguages } from './Command_DiscoverLanguages';
import { Command_ExtractTranslations } from './Command_ExtractTranslations';
import { Command_GenerateOutputCsv } from './Command_GenerateOutputCsv';
import { Command_GroupKeysByModule } from './Command_GroupKeysByModule';
import { Command_ParseInputCsv } from './Command_ParseInputCsv';
import { FileCache } from './FileCache';


// ============================================================================
// BUILDER CLASS - The Main Interface
// ============================================================================

export class IntlAnalyzer {
  private inputCsv?: string;
  private outputCsv?: string;
  private rootPath?: string;
  private moduleRegistry?: ModuleRegistry;
  private targetModules = '';
  private targetModuleAlias: Record<string, string[]> = {};
  private knownGroups: string[] = [];

  withInputCsv(path: string): IntlAnalyzer {
    this.inputCsv = path;
    return this;
  }

  withOutputCsv(path: string): IntlAnalyzer {
    this.outputCsv = path;
    return this;
  }

  withModuleRegistry(registry: ModuleRegistry): IntlAnalyzer {
    this.moduleRegistry = registry;
    return this;
  }

  withTargetModules(targetModules: string): IntlAnalyzer {
    this.targetModules = targetModules;
    return this;
  }

  withTargetModuleAlias(targetModuleAlias: Record<string, string[]>): IntlAnalyzer {
    this.targetModuleAlias = targetModuleAlias;
    return this;
  }
  withKnownGroups(knownGroups: string[]): IntlAnalyzer {
    this.knownGroups = knownGroups;
    return this;
  }
  withRootPath(rootPath: string): IntlAnalyzer {
    this.rootPath = rootPath;
    return this;
  }
  async build(): Promise<Command_GenerateOutputCsv.Result> {
    if (!this.inputCsv) throw new Error('Input CSV path is required');
    if (!this.outputCsv) throw new Error('Output CSV path is required');
    if (!this.moduleRegistry) throw new Error('Module registry is required');
    if (!this.rootPath) throw new Error('rootPath is required');

    console.log('🎯 Starting IntlAnalyzer pipeline...');
    console.log('━'.repeat(50));

    const cache = new FileCache();

    // Execute command pipeline
    const parseResult = await new Command_ParseInputCsv().execute({
      csvPath: this.inputCsv
    });

    const languageResult = new Command_DiscoverLanguages().execute({
      lines: parseResult.lines
    });

    const extractResult = new Command_ExtractTranslations().execute({
      lines: parseResult.lines,
      languages: languageResult.detectedLanguages
    });

    const analysisResult = await new Command_AnalyzeModuleUsage().execute({
      fileCache: cache,
      translationLines: extractResult.translationLines,
      moduleRegistry: this.moduleRegistry,
      targetModules: this.targetModules,
      targetModuleAlias: this.targetModuleAlias,
      knownGroups: this.knownGroups,
      rootPath: this.rootPath
    });

    const groupResult = new Command_GroupKeysByModule().execute({
      analyzedLines: analysisResult.analyzedLines,
      orphanedLines: analysisResult.orphanedLines,
      languages: languageResult.detectedLanguages
    });

    const finalResult = new Command_GenerateOutputCsv().execute({
      moduleGroups: groupResult.moduleGroups,
      orphanedGroup: groupResult.orphanedGroup,
      languages: languageResult.detectedLanguages,
      outputPath: this.outputCsv
    });

    console.log('━'.repeat(50));
    console.log('🎉 IntlAnalyzer pipeline completed successfully!');
    
    return finalResult;
  }
}