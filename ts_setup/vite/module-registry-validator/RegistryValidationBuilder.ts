import { ModuleRegistry, ValidationOptions, ValidationResult } from '../module-registry';
import { Command_ResolveDependencyTree } from './Command_ResolveDependencyTree';
import { Command_GenerateValidationResult } from './Command_GenerateValidationResult';
import { Command_ValidateRegistryStructure } from './Command_ValidateRegistryStructure';
import { Command_ValidateModules } from './Command_ValidateModules';
import { Command_ValidateCircularDependencies } from './Command_ValidateCircularDependencies'



export class RegistryValidationBuilder {
 
 build(registry: ModuleRegistry, targetModule?: string, options: ValidationOptions = {}): ValidationResult {
   const startTime = Date.now();
   // console.log(`🔍 Registry validation starting${targetModule ? ` for target: ${targetModule}` : ' (full registry)'}`);

   try {
     // Command pipeline orchestration
     const validateStructureCmd = new Command_ValidateRegistryStructure();
     const resolveDependencyTreeCmd = new Command_ResolveDependencyTree();
     const validateModulesCmd = new Command_ValidateModules();
     const validateCircularDepsCmd = new Command_ValidateCircularDependencies();
     const generateResultCmd = new Command_GenerateValidationResult();

     // Step 1: Corruption/Sanity checks (fail fast)
     validateStructureCmd.execute({
       registry,
       targetModule
     });
     
     // Step 2: Determine validation scope
     const { modulesToValidate } = resolveDependencyTreeCmd.execute({
       registry,
       targetModule
     });

     // Step 3: Validate individual modules
     const { errors: moduleErrors, warnings: moduleWarnings, corruptions: moduleCorruptions } = validateModulesCmd.execute({
       registry,
       modulesToValidate,
       options
     });

     // Step 4: Validate circular dependencies
     const { errors: circularErrors } = validateCircularDepsCmd.execute({
       registry,
       modulesToValidate
     });

     // Step 5: Generate final result
     const validationTime = Date.now() - startTime;
     const { validationResult } = generateResultCmd.execute({
       targetModule,
       modulesToValidate,
       moduleErrors,
       moduleWarnings,
       moduleCorruptions,
       circularErrors,
       validationTime
     });

     // console.log(`✅ Validation completed in ${validationResult.validationTime}ms`);
     return validationResult;

   } catch (error) {
     // Catastrophic failure - create corruption result
     console.error('💥 Validation corruption detected:', error);
     return this.createCorruptionResult(error, targetModule, Date.now() - startTime);
   }
 }

 private createCorruptionResult(error: any, targetModule?: string, validationTime: number = 0): ValidationResult {
   return {
     targetModule,
     validatedModules: [],
     errors: [],
     warnings: [],
     corruptions: [{
       type: 'corruption_error',
       severity: 'corruption',
       moduleName: targetModule || 'unknown',
       item: 'registry',
       problem: `💥 CATASTROPHIC VALIDATION FAILURE: ${error.message}`,
       solution: `🔧 Registry validation completely failed. Try FORCE_REGISTRY_REBUILD=true`,
       technicalDetails: `Error: ${error.message}\nStack: ${error.stack}\nRegistry keys: ${typeof error.registry === 'object' ? Object.keys(error.registry || {}) : 'unavailable'}`
     }],
     isValid: false,
     isCorrupted: true,
     summary: `💥 CATASTROPHIC FAILURE: Validation could not complete`,
     validationTime
   };
 }
}