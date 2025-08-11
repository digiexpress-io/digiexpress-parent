import { ValidationError, ValidationResult } from "../module-registry";


export declare namespace Command_GenerateValidationResult {
  export interface Input {
    targetModule?: string;
    modulesToValidate: string[];
    moduleErrors: ValidationError[];
    moduleWarnings: ValidationError[];
    moduleCorruptions: ValidationError[];
    circularErrors: ValidationError[];
    validationTime: number;
  }

  export interface Result {
    validationResult: ValidationResult;
  }
}

export class Command_GenerateValidationResult {
  execute(input: Command_GenerateValidationResult.Input): Command_GenerateValidationResult.Result {
    const {
      targetModule,
      modulesToValidate,
      moduleErrors,
      moduleWarnings,
      moduleCorruptions,
      circularErrors,
      validationTime
    } = input;

    // Combine all errors
    const allErrors = [...moduleErrors, ...circularErrors];
    const allWarnings = moduleWarnings;
    const allCorruptions = moduleCorruptions;

    const isCorrupted = allCorruptions.length > 0;
    const isValid = allErrors.length === 0 && !isCorrupted;

    const validationResult: ValidationResult = {
      targetModule,
      validatedModules: modulesToValidate,
      errors: allErrors,
      warnings: allWarnings,
      corruptions: allCorruptions,
      isValid,
      isCorrupted,
      summary: _generateSummary(allErrors, allWarnings, allCorruptions),
      validationTime
    };

    return { validationResult };
  }
}

// Pure transformative functions
function _generateSummary(errors: ValidationError[], warnings: ValidationError[], corruptions: ValidationError[]): string {
  if (corruptions.length > 0) {
    return `💥 VALIDATION CORRUPTED: ${corruptions.length} corruption(s) detected`;
  } else if (errors.length > 0) {
    return `❌ VALIDATION FAILED: ${errors.length} error(s), ${warnings.length} warning(s)`;
  } else if (warnings.length > 0) {
    return `⚠️  VALIDATION PASSED: ${warnings.length} warning(s)`;
  } else {
    return `✅ VALIDATION PASSED: No issues found`;
  }
}
