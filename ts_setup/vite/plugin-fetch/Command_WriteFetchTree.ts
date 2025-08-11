
// ============================================================================
// WRITE FETCH TREE COMMAND
// ============================================================================

import { dirname } from "node:path"
import { GeneratedTree } from "./fetch-types"
import { mkdir, writeFile } from "node:fs/promises"

export declare namespace Command_WriteFetchTree {
  export interface Input {
    generatedTree: GeneratedTree
    outputPath: string
  }
  
  export interface Result {
    writtenFile: string
    fileSize: number
    success: boolean
    error?: string
  }
}

export class Command_WriteFetchTree {
  async execute(input: Command_WriteFetchTree.Input): Promise<Command_WriteFetchTree.Result> {
    try {
      console.log(`📝 Writing fetch tree: ${input.outputPath}`)
      
      // Ensure directory exists
      await mkdir(dirname(input.outputPath), { recursive: true })
      
      // Write file
      await writeFile(input.outputPath, input.generatedTree.content, 'utf-8')
      
      console.log(`✅ Written fetch tree (${input.generatedTree.content.length} chars)`)
      
      return {
        writtenFile: input.outputPath,
        fileSize: input.generatedTree.content.length,
        success: true
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ Failed to write fetch tree to ${input.outputPath}:`, errorMessage)
      
      return {
        writtenFile: input.outputPath,
        fileSize: 0,
        success: false,
        error: errorMessage
      }
    }
  }
}