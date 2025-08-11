
// ============================================================================
// CALCULATE FETCH DIFF COMMAND
// ============================================================================

import { FetchDiff, GeneratedTree } from "./fetch-types"


export declare namespace Command_CalculateFetchDiff {
  export interface Input {
    newTree: GeneratedTree
    existingContent: string | null
  }
  
  export interface Result {
    diff: FetchDiff
    success: boolean
    error?: string
  }
}

export class Command_CalculateFetchDiff {
  execute(input: Command_CalculateFetchDiff.Input): Command_CalculateFetchDiff.Result {
    try {
      console.log(`🔍 Calculating fetch tree diff`)
      
      const hasChanges = input.existingContent !== input.newTree.content
      
      const diff: FetchDiff = {
        hasChanges,
        addedFiles: input.existingContent === null ? input.newTree.sourceFiles : [],
        modifiedFiles: hasChanges && input.existingContent !== null ? [input.newTree.fileName] : [],
        deletedFiles: [], // Not applicable for single generated file
        unchangedFiles: hasChanges ? [] : [input.newTree.fileName],
        previousContent: input.existingContent || undefined,
        newContent: input.newTree.content,
        sourceFilesChanged: input.newTree.sourceFiles // Simplification - could be more sophisticated
      }
      
      if (hasChanges) {
        console.log(`📝 Changes detected in fetch tree`)
      } else {
        console.log(`✅ No changes in fetch tree`)
      }
      
      return {
        diff,
        success: true
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ Failed to calculate diff:`, errorMessage)
      
      return {
        diff: {
          hasChanges: false,
          addedFiles: [],
          modifiedFiles: [],
          deletedFiles: [],
          unchangedFiles: [],
          newContent: input.newTree.content,
          sourceFilesChanged: []
        },
        success: false,
        error: errorMessage
      }
    }
  }
}