import { normalize, resolve } from 'node:path'
import type { Plugin } from 'vite'
import { FetchConfig } from './fetch-types'
import { FileLockManager } from './FileLockManager'
import { Command_ProcessFetchTree } from './Command_ProcessFetchTree'



export interface FetchPluginOptions extends Partial<FetchConfig> {
  verbose?: boolean
  moduleName: string
}

const DEFAULT_CONFIG: Omit<FetchConfig, "moduleName"> = {
  fetchDirectory: "src/fetch",
  fetchTreeDirectory: "src",
  fetchTreeGenFile: "fetchTree.gen.ts",
  routeFileIgnorePattern: "__root",
}

export function fetchVite(options: FetchPluginOptions): Plugin {
  const processCommand = new Command_ProcessFetchTree()
  let rootPath: string
  let config: FetchConfig

  return {
    name: 'fetch-vite',
    
    async configResolved(resolvedConfig) {
      rootPath = process.cwd()

      // Merge user options with defaults
      config = {
        ...DEFAULT_CONFIG,
        ...options
      }
      
      console.log(`⚙️  Fetch plugin configured:`)
      console.log(`   📂 Source: ${config.fetchDirectory}`)
      console.log(`   📄 Output: ${config.fetchTreeDirectory}/${config.fetchTreeGenFile}`)
      console.log(`   🏷️  Prefix: ${config.routeFilePrefix || 'none'}`)
      console.log(`   🚫 Ignore prefix: ${config.routeFileIgnorePrefix || 'none'}`)
      console.log(`   🚫 Ignore pattern: ${config.routeFileIgnorePattern || 'none'}`)
      
      // Generate on startup
      await processFetchTree(config, rootPath, processCommand, options.verbose)
    },
    
    async watchChange(id, { event }) {
      await handleFileChange(id, event, config, rootPath, processCommand, options.verbose)
    }
  }
}

const handleFileChange = async (
  file: string,
  event: 'create' | 'update' | 'delete',
  config: FetchConfig,
  rootPath: string,
  processCommand: Command_ProcessFetchTree,
  verbose?: boolean
  
): Promise<void> => {
  const filePath = normalize(file)
  const fetchDirectoryPath = resolve(rootPath, config.fetchDirectory)
  const generatedFilePath = resolve(rootPath, config.fetchTreeDirectory, config.fetchTreeGenFile)
  
  // Only process if the changed file is in the fetch directory and not the generated file
  if (filePath.startsWith(fetchDirectoryPath) && filePath !== generatedFilePath) {
    console.log(`📁 Fetch file ${event}: ${file.replace(rootPath, '')}`)
    await processFetchTree(config, rootPath, processCommand, verbose)
  }
}

const processFetchTree = async (
  config: FetchConfig,
  rootPath: string,
  processCommand: Command_ProcessFetchTree,
  verbose = false
): Promise<void> => {
  const lockKey = resolve(rootPath, config.fetchTreeDirectory, config.fetchTreeGenFile)
  
  const result = await FileLockManager.withLock(lockKey, async () => {
    return await processCommand.execute({
      config,
      rootPath,
      verbose,
      moduleName: config.moduleName
    })
  })
  
  if (result === null) {
    // File was locked, skip processing
    return
  }
  
  if (result.success) {
    const { diff, summary } = result
    
    if (diff.hasChanges) {
      console.log(`🎉 Fetch tree generation complete with changes:`)
      console.log(`   📄 Generated: ${config.fetchTreeGenFile}`)
      console.log(`   📊 Files processed: ${summary.processedFiles}`)
      console.log(`   📏 Size: ${summary.generatedTreeSize.toLocaleString()} chars`)
      console.log(`   ⏱️  Time: ${summary.processingTimeMs}ms`)
    } else {
      console.log(`✅ Fetch tree generation complete - no changes detected (${summary.processingTimeMs}ms)`)
    }
  } else {
    console.error(`💥 Fetch tree generation failed: ${result.error}`)
  }
}