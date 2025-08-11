import { IntlFile } from "./intl-types"
import { parseCsv } from "./parse-csv"

export declare namespace Command_ParseCsv {
  export interface Input {
    csvFilePath: string
    ignoreErrors: boolean
  }
  
  export interface Result {
    intlFiles: IntlFile[]
    csvFilePath: string
    success: boolean
    error?: string
  }
}

export class Command_ParseCsv {
  async execute(input: Command_ParseCsv.Input): Promise<Command_ParseCsv.Result> {
    try {
      console.log(`🌍 Parsing CSV: ${input.csvFilePath}`)
      
      const intlFiles = await parseCsv(input.csvFilePath, input.ignoreErrors)
      
      return {
        intlFiles,
        csvFilePath: input.csvFilePath,
        success: true
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ CSV parsing failed for ${input.csvFilePath}:`, errorMessage)
      
      return {
        intlFiles: [],
        csvFilePath: input.csvFilePath,
        success: false,
        error: errorMessage
      }
    }
  }
}