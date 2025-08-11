import { readFile, access } from 'node:fs/promises'
import { constants } from 'node:fs'
import { resolve } from 'node:path'

export declare namespace Command_ReadExistingIntlFiles {
  export interface Input {
    outputDirectory: string
    rootPath: string
    expectedFileNames: string[]
  }
  
  export interface Result {
    existingFiles: Map<string, string> // fileName -> content
    success: boolean
    error?: string
  }
}

export class Command_ReadExistingIntlFiles {
  async execute(input: Command_ReadExistingIntlFiles.Input): Promise<Command_ReadExistingIntlFiles.Result> {
    const existingFiles = new Map<string, string>()
    
    try {
      console.log(`📖 Reading existing intl files from ${input.outputDirectory}`)
      
      for (const fileName of input.expectedFileNames) {
        const filePath = resolve(input.rootPath, input.outputDirectory, fileName)
        
        try {
          await access(filePath, constants.F_OK)
          const content = await readFile(filePath, 'utf-8')
          existingFiles.set(fileName, content)
          console.log(`📄 Read existing: ${fileName}`)
        } catch {
          // File doesn't exist, which is fine
          console.log(`🆕 New file detected: ${fileName}`)
        }
      }
      
      return { existingFiles, success: true }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ Failed to read existing intl files:`, errorMessage)
      
      return { existingFiles, success: false, error: errorMessage }
    }
  }
}