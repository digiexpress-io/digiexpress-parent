import { ModuleRegistry } from "../module-registry";
import { AnalyzedLine, Line, ModuleUsage } from "./intl-types";
import { Command_AnalyzeOneModuleUsage } from "./Command_AnalyzeOneModuleUsage";
import { FileCache } from "./FileCache";
import { Command_AnalyzeOneFileUsage_Phase1 } from "./Command_AnalyzeOneFileUsage_Phase1";
import { Command_AnalyzeOneFileUsage_Phase2 } from "./Command_AnalyzeOneFileUsage_Phase2";
import { Command_AnalyzeOneFileUsage_Phase3 } from "./Command_AnalyzeOneFileUsage_Phase3";

export declare namespace Command_AnalyzeModuleUsage {
  export interface Input {
    translationLines: Line[];
    targetModules: string;
    targetModuleAlias: Record<string, string[]>;
    moduleRegistry: ModuleRegistry;
    fileCache: FileCache;
    knownGroups: string[];
  }
  export interface Result {
    analyzedLines: AnalyzedLine[];
    orphanedLines: Line[];
  }
}


export class Command_AnalyzeModuleUsage {
  // Stateful visitor state
  private resolvedKeys = new Map<string, ModuleUsage[]>();
  private orphanedKeys = new Set<string>();
  private translationLineMap = new Map<string, Line>();
  
  // Phase statistics
  private phaseStats = {
    phase1: { found: 0, processed: 0 },
    phase2: { found: 0, processed: 0 },
    phase3: { found: 0, processed: 0 }
  };

  async execute(input: Command_AnalyzeModuleUsage.Input): Promise<Command_AnalyzeModuleUsage.Result> {
    console.log(`🔍 Analyzing module usage for ${input.translationLines.length} translation lines`);
    
    // Initialize state
    this.initializeState(input.translationLines);
    
    // Progressive phase execution
    await this.executePhase1(input);
    await this.executePhase2(input);
    await this.executePhase3(input);
    
    // Build final result
    return this.buildFinalResult();
  }

  private initializeState(translationLines: Line[]): void {
    // Reset state
    this.resolvedKeys.clear();
    this.orphanedKeys.clear();
    this.translationLineMap.clear();
    this.phaseStats = {
      phase1: { found: 0, processed: 0 },
      phase2: { found: 0, processed: 0 },
      phase3: { found: 0, processed: 0 }
    };

    // Build translation key index
    for (const line of translationLines) {
      if (line.type === 'translations' && line.id) {
        this.orphanedKeys.add(line.id);
        this.translationLineMap.set(line.id, line);
        this.resolvedKeys.set(line.id, []); // Initialize with empty array
      }
    }

    console.log(`  📋 Initialized ${this.orphanedKeys.size} translation keys`);
  }

  private async executePhase1(input: Command_AnalyzeModuleUsage.Input): Promise<void> {
    if (this.orphanedKeys.size === 0) return;

    console.log(`🎯 Phase 1: Regex pattern matching (${this.orphanedKeys.size} keys)`);
    const startTime = Date.now();
    const initialOrphans = this.orphanedKeys.size;

    const phase1Results = await this.scanAllModulesForIntlUsage(
      input.fileCache,
      this.orphanedKeys,
      input.moduleRegistry,
      input.targetModules,
      input.targetModuleAlias,
      new Command_AnalyzeOneFileUsage_Phase1()
    );

    this.mergePhaseResults(phase1Results);
    this.updateOrphanedKeys();

    const duration = Date.now() - startTime;
    this.phaseStats.phase1.processed = initialOrphans;
    this.phaseStats.phase1.found = initialOrphans - this.orphanedKeys.size;

    console.log(`  ✅ Phase 1 complete: Found ${this.phaseStats.phase1.found}/${initialOrphans} keys in ${duration}ms, ${this.orphanedKeys.size} remain`);
  }

  private async executePhase2(input: Command_AnalyzeModuleUsage.Input): Promise<void> {
    if (this.orphanedKeys.size === 0) return;

    console.log(`🎯 Phase 2: Brute force quoted search (${this.orphanedKeys.size} keys)`);
    const startTime = Date.now();
    const initialOrphans = this.orphanedKeys.size;

    const phase2Results = await this.scanAllModulesForIntlUsage(
      input.fileCache,
      this.orphanedKeys,
      input.moduleRegistry,
      input.targetModules,
      input.targetModuleAlias,
      new Command_AnalyzeOneFileUsage_Phase2()
    );

    this.mergePhaseResults(phase2Results);
    this.updateOrphanedKeys();

    const duration = Date.now() - startTime;
    this.phaseStats.phase2.processed = initialOrphans;
    this.phaseStats.phase2.found = initialOrphans - this.orphanedKeys.size;

    console.log(`  ✅ Phase 2 complete: Found ${this.phaseStats.phase2.found}/${initialOrphans} keys in ${duration}ms, ${this.orphanedKeys.size} remain`);
  }

  private async executePhase3(input: Command_AnalyzeModuleUsage.Input): Promise<void> {
    if (this.orphanedKeys.size === 0) return;

    console.log(`🎯 Phase 3: Fragment explosion analysis (${this.orphanedKeys.size} keys)`);
    const startTime = Date.now();
    const initialOrphans = this.orphanedKeys.size;

    const phase3Results = await this.scanAllModulesForIntlUsage(
      input.fileCache,
      this.orphanedKeys,
      input.moduleRegistry,
      input.targetModules,
      input.targetModuleAlias,
      new Command_AnalyzeOneFileUsage_Phase3(input.knownGroups)
    );

    this.mergePhaseResults(phase3Results);
    this.updateOrphanedKeys();


    const duration = Date.now() - startTime;
    this.phaseStats.phase3.processed = initialOrphans;
    this.phaseStats.phase3.found = 0; // TODO: Update when Phase 3 is implemented

    console.log(`  ⏭️  Phase 3 skipped (not implemented): ${this.orphanedKeys.size} keys remain orphaned`);
  }

  private mergePhaseResults(phaseResults: Map<string, ModuleUsage[]>): void {
    for (const [key, newUsages] of phaseResults.entries()) {
      if (newUsages.length > 0) {
        const existingUsages = this.resolvedKeys.get(key) || [];
        this.resolvedKeys.set(key, [...existingUsages, ...newUsages]);
      }
    }
  }

  private updateOrphanedKeys(): void {
    // Remove keys that now have usage from orphaned set
    for (const key of Array.from(this.orphanedKeys)) {
      const usage = this.resolvedKeys.get(key);
      if (usage && usage.length > 0) {
        this.orphanedKeys.delete(key);
      }
    }
  }

  private async scanAllModulesForIntlUsage(
    fileCache: FileCache,
    translationKeys: Set<string>,
    moduleRegistry: ModuleRegistry,
    targetModule: string,
    targetModuleAlias: Record<string, string[]>,
    phaseCommand: any // Strategy command for this phase
  ): Promise<Map<string, ModuleUsage[]>> {
    console.log(`  🔎 Scanning ${Object.keys(moduleRegistry.modules).length} modules with ${translationKeys.size} keys...`);
    
    const keyUsageMap = new Map<string, ModuleUsage[]>();

    // Initialize empty arrays for all keys being processed
    for (const key of translationKeys) {
      keyUsageMap.set(key, []);
    }

    // Scan each module
    const moduleFilter = [...targetModuleAlias[targetModule] ?? [], targetModule];
    for (const [moduleName, moduleInfo] of Object.entries(moduleRegistry.modules)) {
      const isScan = moduleFilter.find(target => moduleName.startsWith(target));
      if (!isScan) {
        continue;
      }

      console.log(`    📂 Scanning module: ${moduleName}`);
      
      const moduleUsage = await new Command_AnalyzeOneModuleUsage().execute({
        fileCache,
        moduleName,
        moduleInfo,
        translationKeys,
        rootPath: moduleRegistry.rootPath,
        command: phaseCommand
      });

      // Merge findings into the usage map
      for (const [key, usage] of moduleUsage.entries()) {
        if (usage.length > 0) {
          const existingUsage = keyUsageMap.get(key) || [];
          keyUsageMap.set(key, [...existingUsage, ...usage]);
        }
      }
    }
    return keyUsageMap;
  }

  private buildFinalResult(): Command_AnalyzeModuleUsage.Result {
    const analyzedLines: AnalyzedLine[] = [];
    const orphanedLines: Line[] = [];

    // Process all translation keys
    for (const [key, line] of this.translationLineMap.entries()) {
      const usage = this.resolvedKeys.get(key);
      
      if (usage && usage.length > 0) {
        analyzedLines.push({
          source: line,
          found: usage
        });
      } else {
        orphanedLines.push(line);
      }
    }

    // Log final statistics
    const totalKeys = this.translationLineMap.size;
    const totalFound = this.phaseStats.phase1.found + this.phaseStats.phase2.found + this.phaseStats.phase3.found;
    
    console.log(`📊 Multi-phase analysis complete:`);
    console.log(`  Phase 1 (Regex): ${this.phaseStats.phase1.found} keys`);
    console.log(`  Phase 2 (Brute): ${this.phaseStats.phase2.found} keys`);
    console.log(`  Phase 3 (Fragment): ${this.phaseStats.phase3.found} keys`);
    console.log(`  Total found: ${totalFound}/${totalKeys} (${((totalFound/totalKeys)*100).toFixed(1)}%)`);
    console.log(`✅ Analyzed ${analyzedLines.length} keys, found ${orphanedLines.length} orphans`);

    return { analyzedLines, orphanedLines };
  }
}
