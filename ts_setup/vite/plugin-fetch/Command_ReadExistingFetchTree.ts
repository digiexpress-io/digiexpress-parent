

// ============================================================================
// READ EXISTING TREE COMMAND
// ============================================================================

import { access, constants, readFile } from "node:fs/promises"


export declare namespace Command_ReadExistingFetchTree {
  export interface Input {
    treeFilePath: string
  }
  
  export interface Result {
    existingContent: string | null
    exists: boolean
    success: boolean
    error?: string
  }
}

export class Command_ReadExistingFetchTree {
  async execute(input: Command_ReadExistingFetchTree.Input): Promise<Command_ReadExistingFetchTree.Result> {
    try {
      console.log(`📖 Reading existing fetch tree: ${input.treeFilePath}`)
      
      try {
        await access(input.treeFilePath, constants.F_OK)
        const content = await readFile(input.treeFilePath, 'utf-8')
        
        console.log(`📄 Found existing tree file (${content.length} chars)`)
        
        return {
          existingContent: content,
          exists: true,
          success: true
        }
      } catch {
        console.log(`🆕 No existing tree file found`)
        
        return {
          existingContent: null,
          exists: false,
          success: true
        }
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ Failed to read existing tree file:`, errorMessage)
      
      return {
        existingContent: null,
        exists: false,
        success: false,
        error: errorMessage
      }
    }
  }
}