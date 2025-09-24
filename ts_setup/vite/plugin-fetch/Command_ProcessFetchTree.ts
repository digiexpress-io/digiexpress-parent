import { FetchConfig, FetchDiff, FetchSummary } from './fetch-types'
import { Command_ReadFetchDirectory } from './Command_ReadFetchDirectory'
import { Command_GenerateFetchTree } from './Command_GenerateFetchTree'
import { Command_ReadExistingFetchTree } from './Command_ReadExistingFetchTree'
import { Command_CalculateFetchDiff } from './Command_CalculateFetchDiff'
import { Command_WriteFetchTree } from './Command_WriteFetchTree'
import { Command_ReportFetchChanges } from './Command_ReportFetchChanges'
import { resolve } from 'node:path'


// ============================================================================
// MASTER ORCHESTRATOR COMMAND
// ============================================================================

export declare namespace Command_ProcessFetchTree {
  export interface Input {
    config: FetchConfig
    rootPath: string
    verbose?: boolean
    moduleName: string
  }
  
  export interface Result {
    config: FetchConfig
    diff: FetchDiff
    summary: FetchSummary
    writtenFile?: string
    success: boolean
    error?: string
  }
}

export class Command_ProcessFetchTree {
  private readonly readDirCommand = new Command_ReadFetchDirectory()
  private readonly generateCommand = new Command_GenerateFetchTree()
  private readonly readExistingCommand = new Command_ReadExistingFetchTree()
  private readonly diffCommand = new Command_CalculateFetchDiff()
  private readonly writeCommand = new Command_WriteFetchTree()
  private readonly reportCommand = new Command_ReportFetchChanges()
  
  async execute(input: Command_ProcessFetchTree.Input): Promise<Command_ProcessFetchTree.Result> {
    try {
      console.log(`🚀 Processing fetch tree generation`)
      
      // Step 1: Read fetch directory
      const readResult = await this.readDirCommand.execute({
        fetchDirectory: input.config.fetchDirectory,
        rootPath: input.rootPath,
        config: {
          routeFilePrefix: input.config.routeFilePrefix,
          routeFileIgnorePrefix: input.config.routeFileIgnorePrefix,
          routeFileIgnorePattern: input.config.routeFileIgnorePattern
        }
      })
      
      if (!readResult.success) {
        return this.createErrorResult(input, readResult.error || 'Failed to read directory')
      }
      
      // Step 2: Generate tree
      const generateResult = this.generateCommand.execute({
        sourceFiles: readResult.sourceFiles,
        config: input.config,
        moduleName: input.moduleName
      })
      
      if (!generateResult.success) {
        return this.createErrorResult(input, generateResult.error || 'Failed to generate tree')
      }
      
      // Step 3: Read existing file
      const treeFilePath = resolve(input.rootPath, input.config.fetchTreeDirectory, input.config.fetchTreeGenFile)
      const existingResult = await this.readExistingCommand.execute({ treeFilePath })
      
      if (!existingResult.success) {
        console.warn(`⚠️  Could not read existing file: ${existingResult.error}`)
      }
      
      // Step 4: Calculate diff
      const diffResult = this.diffCommand.execute({
        newTree: generateResult.generatedTree,
        existingContent: existingResult.existingContent
      })
      
      if (!diffResult.success) {
        return this.createErrorResult(input, diffResult.error || 'Failed to calculate diff')
      }
      
      // Step 5: Report changes
      this.reportCommand.execute({
        diff: diffResult.diff,
        summary: generateResult.summary,
        config: input.config,
        verbose: input.verbose
      })
      
      // Step 6: Write file if there are changes
      let writtenFile: string | undefined
      
      if (diffResult.diff.hasChanges) {
        const writeResult = await this.writeCommand.execute({
          generatedTree: generateResult.generatedTree,
          outputPath: treeFilePath
        })
        
        if (!writeResult.success) {
          return {
            config: input.config,
            diff: diffResult.diff,
            summary: generateResult.summary,
            success: false,
            error: writeResult.error
          }
        }
        
        writtenFile = writeResult.writtenFile
      } else {
        console.log('✅ No changes detected - skipping file write')
      }
      
      return {
        config: input.config,
        diff: diffResult.diff,
        summary: generateResult.summary,
        writtenFile,
        success: true
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      return this.createErrorResult(input, errorMessage)
    }
  }
  
  private createErrorResult(input: Command_ProcessFetchTree.Input, error: string): Command_ProcessFetchTree.Result {
    return {
      config: input.config,
      diff: {
        hasChanges: false,
        addedFiles: [],
        modifiedFiles: [],
        deletedFiles: [],
        unchangedFiles: [],
        newContent: '',
        sourceFilesChanged: []
      },
      summary: {
        totalSourceFiles: 0,
        processedFiles: 0,
        skippedFiles: 0,
        generatedTreeSize: 0,
        processingTimeMs: 0,
        lastGenerated: new Date()
      },
      success: false,
      error
    }
  }
}