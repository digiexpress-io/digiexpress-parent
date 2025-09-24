
import { dirname, resolve } from "node:path"
import { IntlFile } from "./intl-types"
import { mkdir, writeFile } from "node:fs/promises"

export declare namespace Command_WriteIntlFiles {
  export interface Input {
    intlFiles: IntlFile[]
    outputDirectory: string
    rootPath: string
  }
  
  export interface Result {
    writtenFiles: string[]
    outputDirectory: string
    success: boolean
    error?: string
  }
}

export class Command_WriteIntlFiles {
  async execute(input: Command_WriteIntlFiles.Input): Promise<Command_WriteIntlFiles.Result> {
    const writtenFiles: string[] = []
    
    try {
      console.log(`📝 Writing ${input.intlFiles.length} intl files to ${input.outputDirectory}`)
      
      for (const intlFile of input.intlFiles) {
        const outputPath = resolve(input.rootPath, input.outputDirectory, intlFile.fileName)
        
        // Ensure directory exists
        await mkdir(dirname(outputPath), { recursive: true })
        
        // Write file
        await writeFile(outputPath, intlFile.content, 'utf-8')
        writtenFiles.push(outputPath)
        
        console.log(`✅ Written: ${intlFile.fileName}`)
      }
      
      return {
        writtenFiles,
        outputDirectory: input.outputDirectory,
        success: true
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ Failed to write intl files to ${input.outputDirectory}:`, errorMessage)
      
      return {
        writtenFiles,
        outputDirectory: input.outputDirectory,
        success: false,
        error: errorMessage
      }
    }
  }
}