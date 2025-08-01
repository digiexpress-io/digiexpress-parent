



// ============================================================================
// ENHANCED PROCESS COMMAND WITH DIFFING
// ============================================================================

import { Command_ReadExistingIntlFiles } from "./Command_ReadExistingIntlFiles"
import { Command_ParseCsv } from "./Command_ParseCsv"
import { Command_CalculateIntlDiff } from "./Command_CalculateIntlDiff"
import { Command_WriteIntlFiles } from "./Command_WriteIntlFiles"
import { Command_ReportIntlDiff } from "./Command_ReportIntlDiff"

import { DiffSummary, IntlFileDiff } from "./intl-types"

export declare namespace Command_ProcessIntlCsvWithDiff {
  export interface Input {
    csvFilePath: string
    outputDirectory: string
    rootPath: string
    ignoreErrors: boolean
    verbose?: boolean
  }
  
  export interface Result {
    csvFilePath: string
    outputDirectory: string
    writtenFiles: string[]
    diffs: IntlFileDiff[]
    summary: DiffSummary
    success: boolean
    error?: string
  }
}

export class Command_ProcessIntlCsvWithDiff {
  private readonly parseCommand = new Command_ParseCsv()
  private readonly readCommand = new Command_ReadExistingIntlFiles()
  private readonly diffCommand = new Command_CalculateIntlDiff()
  private readonly writeCommand = new Command_WriteIntlFiles()
  private readonly reportCommand = new Command_ReportIntlDiff()
  
  async execute(input: Command_ProcessIntlCsvWithDiff.Input): Promise<Command_ProcessIntlCsvWithDiff.Result> {
    try {
      console.log(`🚀 Processing intl CSV with diff tracking: ${input.csvFilePath}`)
      
      // Step 1: Parse CSV
      const parseResult = await this.parseCommand.execute({
        csvFilePath: input.csvFilePath,
        ignoreErrors: input.ignoreErrors
      })
      
      if (!parseResult.success) {
        return {
          csvFilePath: input.csvFilePath,
          outputDirectory: input.outputDirectory,
          writtenFiles: [],
          diffs: [],
          summary: this.createEmptySummary(),
          success: false,
          error: parseResult.error
        }
      }
      
      // Step 2: Read existing files
      const expectedFileNames = parseResult.intlFiles.map(f => f.fileName)
      const readResult = await this.readCommand.execute({
        outputDirectory: input.outputDirectory,
        rootPath: input.rootPath,
        expectedFileNames
      })
      
      if (!readResult.success) {
        console.warn(`⚠️  Could not read existing files: ${readResult.error}`)
        // Continue with empty existing files
      }
      
      // Step 3: Calculate diffs
      const diffResult = this.diffCommand.execute({
        newFiles: parseResult.intlFiles,
        existingFiles: readResult.existingFiles
      })
      
      if (!diffResult.success) {
        return {
          csvFilePath: input.csvFilePath,
          outputDirectory: input.outputDirectory,
          writtenFiles: [],
          diffs: [],
          summary: this.createEmptySummary(),
          success: false,
          error: diffResult.error
        }
      }
      
      // Step 4: Report changes
      this.reportCommand.execute({
        diffs: diffResult.diffs,
        summary: diffResult.summary,
        verbose: input.verbose
      })
      
      // Step 5: Write files (only if there are changes)
      const hasChanges = diffResult.diffs.some(d => d.changeType !== 'unchanged')
      let writtenFiles: string[] = []
      
      if (hasChanges) {
        const writeResult = await this.writeCommand.execute({
          intlFiles: parseResult.intlFiles,
          outputDirectory: input.outputDirectory,
          rootPath: input.rootPath
        })
        
        if (!writeResult.success) {
          return {
            csvFilePath: input.csvFilePath,
            outputDirectory: input.outputDirectory,
            writtenFiles: writeResult.writtenFiles,
            diffs: diffResult.diffs,
            summary: diffResult.summary,
            success: false,
            error: writeResult.error
          }
        }
        
        writtenFiles = writeResult.writtenFiles
      } else {
        console.log('✅ No changes detected - skipping file writes')
      }
      
      return {
        csvFilePath: input.csvFilePath,
        outputDirectory: input.outputDirectory,
        writtenFiles,
        diffs: diffResult.diffs,
        summary: diffResult.summary,
        success: true
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      
      return {
        csvFilePath: input.csvFilePath,
        outputDirectory: input.outputDirectory,
        writtenFiles: [],
        diffs: [],
        summary: this.createEmptySummary(),
        success: false,
        error: errorMessage
      }
    }
  }
  
  private createEmptySummary(): DiffSummary {
    return {
      totalFiles: 0, filesAdded: 0, filesModified: 0, filesDeleted: 0, filesUnchanged: 0,
      totalKeys: 0, keysAdded: 0, keysModified: 0, keysDeleted: 0,
      localesAffected: [],
      missingTranslations: [],
      translationCoverage: {
        totalKeys: 0, totalExpectedTranslations: 0, actualTranslations: 0,
        coveragePercentage: 100, incompleteKeys: 0
      },
      processingTimeMs: 0
    }
  }
}
