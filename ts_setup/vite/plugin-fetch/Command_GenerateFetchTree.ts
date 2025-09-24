
// ============================================================================
// GENERATE FETCH TREE COMMAND
// ============================================================================

import { FetchConfig, SourceFile, FetchSummary, GeneratedTree } from "./fetch-types"
import { parseTree } from "./get-tree-visitor"


export declare namespace Command_GenerateFetchTree {
  export interface Input {
    sourceFiles: SourceFile[]
    config: FetchConfig
    moduleName: string
  }
  
  export interface Result {
    generatedTree: GeneratedTree
    summary: FetchSummary
    success: boolean
    error?: string
  }
}

export class Command_GenerateFetchTree {
  execute(input: Command_GenerateFetchTree.Input): Command_GenerateFetchTree.Result {
    const startTime = Date.now()
    
    try {
      console.log(`🌳 Generating fetch tree from ${input.sourceFiles.length} files`)
      
      // Extract file paths for the parseTree function
      const filePaths = input.sourceFiles.map(f => f.fileName)
      
      console.log(`📊 Processing ${filePaths.length} source files`)
      
      // Generate tree using existing parseTree function
      const treeResults = parseTree(input.moduleName, input.sourceFiles)
      
      if (!treeResults || treeResults.length === 0) {
        throw new Error('parseTree returned no results')
      }
      
      // Assuming parseTree returns array with single result for now
      const treeContent = treeResults[0]?.content || ''
      
      const generatedTree: GeneratedTree = {
        content: treeContent,
        fileName: input.config.fetchTreeGenFile,
        sourceFiles: filePaths,
        generatedAt: new Date()
      }
      
      const processingTime = Date.now() - startTime
      
      const summary: FetchSummary = {
        totalSourceFiles: input.sourceFiles.length,
        processedFiles: filePaths.length,
        skippedFiles: 0, // All source files are processed
        generatedTreeSize: treeContent.length,
        processingTimeMs: processingTime,
        lastGenerated: generatedTree.generatedAt
      }
      
      console.log(`✅ Generated fetch tree (${treeContent.length} chars) in ${processingTime}ms`)
      
      return {
        generatedTree,
        summary,
        success: true
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ Failed to generate fetch tree:`, errorMessage)
      
      const processingTime = Date.now() - startTime
      
      return {
        generatedTree: {
          content: '',
          fileName: input.config.fetchTreeGenFile,
          sourceFiles: [],
          generatedAt: new Date()
        },
        summary: {
          totalSourceFiles: 0,
          processedFiles: 0,
          skippedFiles: 0,
          generatedTreeSize: 0,
          processingTimeMs: processingTime,
          lastGenerated: new Date()
        },
        success: false,
        error: errorMessage
      }
    }
  }
}