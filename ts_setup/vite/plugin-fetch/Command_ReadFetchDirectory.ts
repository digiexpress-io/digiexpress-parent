
import { posix, resolve } from 'node:path'
import { readdir, stat, access } from 'node:fs/promises'
import { constants, readdirSync } from 'node:fs'
import { SourceFile } from './fetch-types'

// ============================================================================
// READ FETCH DIRECTORY COMMAND
// ============================================================================

export declare namespace Command_ReadFetchDirectory {
  export interface Input {
    fetchDirectory: string
    rootPath: string
    config: {
      routeFilePrefix?: string
      routeFileIgnorePrefix?: string
      routeFileIgnorePattern?: string
    }
  }
  
  export interface Result {
    sourceFiles: SourceFile[]
    fetchDirectory: string
    success: boolean
    error?: string
  }
}

export class Command_ReadFetchDirectory {
  async execute(input: Command_ReadFetchDirectory.Input): Promise<Command_ReadFetchDirectory.Result> {
    try {
      console.log(`📂 Reading fetch directory: ${input.fetchDirectory}`)
      
      const fetchDirPath = resolve(input.rootPath, input.fetchDirectory)
      
      // Check if directory exists
      try {
        await access(fetchDirPath, constants.F_OK)
      } catch {
        console.warn(`⚠️  Fetch directory does not exist: ${fetchDirPath}`)
        return {
          sourceFiles: [],
          fetchDirectory: input.fetchDirectory,
          success: true // Not an error, just empty
        }
      }
      
      // Use the existing readDirectory logic (improved version)
      const sourceFiles = this.readDirectory(fetchDirPath, input.config)
      
      console.log(`📄 Found ${sourceFiles.length} fetch files`)
      
      return {
        sourceFiles,
        fetchDirectory: input.fetchDirectory,
        success: true
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ Failed to read fetch directory ${input.fetchDirectory}:`, errorMessage)
      
      return {
        sourceFiles: [],
        fetchDirectory: input.fetchDirectory,
        success: false,
        error: errorMessage
      }
    }
  }
  
  private readDirectory(
    fullPath: string,
    config: {
      routeFilePrefix?: string
      routeFileIgnorePrefix?: string
      routeFileIgnorePattern?: string
    }
  ): SourceFile[] {
    const { routeFilePrefix, routeFileIgnorePrefix, routeFileIgnorePattern } = config
    const routeFileIgnoreRegExp = routeFileIgnorePattern ? new RegExp(routeFileIgnorePattern, 'g') : null
    
    console.log(`🔍 Scanning directory: ${fullPath}`)
    
    return readdirSync(fullPath, { withFileTypes: true, recursive: false })
      .filter((dirent) => {
        const name = dirent.name
        console.log(`📋 Evaluating file: ${name}`)
        
        // Skip hidden files and ignored prefixes
        if (name.startsWith('.')) {
          console.log(`   ⏭️  Skipping hidden file: ${name}`)
          return false
        }
        
        if (routeFileIgnorePrefix && name.startsWith(routeFileIgnorePrefix)) {
          console.log(`   ⏭️  Skipping ignored prefix: ${name}`)
          return false
        }
        
        // Apply prefix requirement
        if (routeFilePrefix && !name.startsWith(routeFilePrefix)) {
          console.log(`   ⏭️  Skipping (missing required prefix): ${name}`)
          return false
        }
        
        // Apply ignore pattern
        if (routeFileIgnoreRegExp && name.match(routeFileIgnoreRegExp)) {
          console.log(`   ⏭️  Skipping (matches ignore pattern): ${name}`)
          return false
        }
        
        return true
      })
      .filter(dirent => {
        const fileName = posix.join(fullPath, dirent.name)
        const isValidExtension = fileName.match(/\.(tsx|ts|jsx|js)$/)
        
        if (!isValidExtension) {
          console.log(`   ⏭️  Skipping (invalid extension): ${dirent.name}`)
          return false
        }
        
        console.log(`   ✅ Including file: ${dirent.name}`)
        return true
      })
      .map((dirent) => {
        const fileName = posix.join(fullPath, dirent.name)
        const relativePath = posix.join('./', dirent.name)
        
        return {
          fileName,
          relativePath,
          dirent
        }
      })
  }
}
