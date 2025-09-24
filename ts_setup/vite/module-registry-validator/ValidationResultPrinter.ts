import { ValidationError, ValidationResult } from "../module-registry";

export class ValidationResultPrinter {
  
  print(result: ValidationResult): string {
    const sections: string[] = [];
    
    // Header
    sections.push(this.printHeader(result));
    sections.push('');

    // Corruptions first (highest priority)
    if (result.corruptions.length > 0) {
      sections.push(this.printCorruptions(result.corruptions));
      sections.push('');
    }

    // Errors
    if (result.errors.length > 0) {
      sections.push(this.printErrors(result.errors));
      sections.push('');
    }

    // Warnings
    if (result.warnings.length > 0) {
      sections.push(this.printWarnings(result.warnings));
      sections.push('');
    }

    // Summary footer
    sections.push(this.printSummary(result));

    return sections.join('\n');
  }

  private printHeader(result: ValidationResult): string {
    const target = result.targetModule ? ` for ${result.targetModule}` : ' (full registry)';
    const moduleCount = result.validatedModules.length;
    const time = result.validationTime;
    
    return `🔍 Registry Validation Results${target}\n📊 Validated ${moduleCount} modules in ${time}ms`;
  }

  private printCorruptions(corruptions: ValidationError[]): string {
    const lines: string[] = [];
    lines.push(`💥 CORRUPTIONS (${corruptions.length}):`);
    
    for (const corruption of corruptions) {
      lines.push(`   💥 ${corruption.moduleName.padEnd(30)} // ${corruption.problem}`);
      lines.push(`      🔧 ${corruption.solution}`);
      
      if (corruption.technicalDetails) {
        lines.push(`      📋 Technical: ${corruption.technicalDetails.split('\n')[0]}`);
      }
      lines.push('');
    }
    
    return lines.join('\n');
  }

  private printErrors(errors: ValidationError[]): string {
    const lines: string[] = [];
    lines.push(`❌ ERRORS (${errors.length}):`);
    
    // Group errors by type
    const groupedErrors = this.groupErrorsByType(errors);
    
    for (const [type, typeErrors] of Object.entries(groupedErrors)) {
      lines.push(`   📋 ${this.getTypeDisplayName(type)}:`);
      
      for (const error of typeErrors) {
        const item = error.item;
        lines.push(`      ❌ ${item} // ${error.problem}`);
        lines.push(`         💡 ${error.solution}`);
      }
      lines.push('');
    }
    
    return lines.join('\n');
  }

  private printWarnings(warnings: ValidationError[]): string {
    const lines: string[] = [];
    lines.push(`⚠️  WARNINGS (${warnings.length}):`);
    
    for (const warning of warnings) {
      const item = warning.item;
      lines.push(`   ⚠️  ${item} // ${warning.problem}`);
      lines.push(`      💡 ${warning.solution}`);
    }
    
    return lines.join('\n');
  }

  private printSummary(result: ValidationResult): string {
    const lines: string[] = [];
    lines.push('━'.repeat(60));
    lines.push(result.summary);
    
    if (!result.isValid && !result.isCorrupted) {
      lines.push('🔧 Fix the errors above and rebuild the registry.');
    } else if (result.isCorrupted) {
      lines.push('💥 Registry is corrupted. Run: FORCE_REGISTRY_REBUILD=true');
    }
    
    return lines.join('\n');
  }

  private groupErrorsByType(errors: ValidationError[]): Record<string, ValidationError[]> {
    const groups: Record<string, ValidationError[]> = {};
    
    for (const error of errors) {
      if (!groups[error.type]) {
        groups[error.type] = [];
      }
      groups[error.type].push(error);
    }
    
    return groups;
  }

  private getTypeDisplayName(type: string): string {
    const names: Record<string, string> = {
      'missing_external': 'Missing External Dependencies',
      'unused_external': 'Unused External Dependencies',
      'missing_internal': 'Missing Internal Dependencies', 
      'unused_internal': 'Unused Internal Dependencies',
      'circular_dependency': 'Circular Dependencies',
      'missing_module': 'Missing Modules',
      'invalid_structure': 'Invalid Module Structure'
    };
    
    return names[type] || type.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  }
}