import { AnalyzedLine, Line } from "./intl-types";

export declare namespace Command_GroupKeysByModule {
  export interface Input {
    analyzedLines: AnalyzedLine[];
    orphanedLines: Line[];
    languages: string[];
  }
  export interface Result {
    moduleGroups: Record<string, AnalyzedLine[]>;
    orphanedGroup: Line[];
  }
}

export class Command_GroupKeysByModule {
  execute(input: Command_GroupKeysByModule.Input): Command_GroupKeysByModule.Result {
    console.log(`📦 Grouping keys by modules`);
    
    const moduleGroups: Record<string, AnalyzedLine[]> = {};
    const orphanedGroup: Line[] = [];

    // Process analyzed lines (keys with known usage)
    for (const analyzedLine of input.analyzedLines) {
      if (analyzedLine.source.type !== 'translations' || !analyzedLine.source.id) continue;

      // Group by first module found (could be enhanced to handle multiple modules)
      const primaryModule = analyzedLine.found[0]?.moduleName;
      
      if (primaryModule) {
        if (!moduleGroups[primaryModule]) {
          moduleGroups[primaryModule] = [];
        }
        moduleGroups[primaryModule].push(analyzedLine);
      } else {
        // If analyzed but no modules found, treat as orphaned
        orphanedGroup.push(analyzedLine.source);
      }
    }

    // Process orphaned lines - add them directly
    for (const orphanedLine of input.orphanedLines) {
      orphanedGroup.push(orphanedLine);
    }

    // Sort each module group by key
    for (const group of Object.values(moduleGroups)) {
      group.sort((a, b) => {
        const keyA = a.source.type === 'translations' ? a.source.id : '';
        const keyB = b.source.type === 'translations' ? b.source.id : '';
        return keyA.localeCompare(keyB);
      });
    }

    // Sort orphaned group by key where possible
    orphanedGroup.sort((a, b) => {
      return a.lineNumber - b.lineNumber;
    });

    console.log(`✅ Created ${Object.keys(moduleGroups).length} module groups, ${orphanedGroup.length} orphaned items`);
    return { moduleGroups, orphanedGroup };
  }
}