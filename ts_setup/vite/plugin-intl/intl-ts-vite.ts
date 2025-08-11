import { normalize, resolve } from 'node:path'
import type { Plugin } from 'vite'
import { Command_ProcessIntlCsvWithDiff } from './Command_ProcessIntlCsvWithDiff'
import { FileLockManager } from './FileLockManager'

export interface Config {
  intlDirectory: string
  csv: string
  ignoreErrors?: boolean
  verbose?: boolean
}

export function generateIntl(options: Config = {
  intlDirectory: "src/intl",
  csv: "intl.csv",
  ignoreErrors: false,
  verbose: false
}): Plugin {
  const rootPath = process.cwd()
  const processCommand = new Command_ProcessIntlCsvWithDiff()

  return {
    name: 'generate-intl',
    
    async watchChange(id, { event }) {
      await handleFileChange(id, event, options, rootPath, processCommand)
    },
    
    async configResolved() {
      const csvFilePath = resolve(rootPath, options.csv)
      await processIntlFile(csvFilePath, options, rootPath, processCommand)
    }
  }
}

const handleFileChange = async (
  file: string,
  event: 'create' | 'update' | 'delete',
  options: Config,
  rootPath: string,
  processCommand: Command_ProcessIntlCsvWithDiff
): Promise<void> => {
  const filePath = normalize(file)
  const csvPath = resolve(rootPath, options.csv)
  
  if (filePath === csvPath) {
    console.log(`📁 CSV file ${event}: ${options.csv}`)
    await processIntlFile(csvPath, options, rootPath, processCommand)
  }
}

const processIntlFile = async (
  csvFilePath: string,
  options: Config,
  rootPath: string,
  processCommand: Command_ProcessIntlCsvWithDiff
): Promise<void> => {
  const result = await FileLockManager.withLock(csvFilePath, async () => {
    return await processCommand.execute({
      csvFilePath,
      outputDirectory: options.intlDirectory,
      rootPath,
      ignoreErrors: options.ignoreErrors ?? false,
      verbose: options.verbose ?? false
    })
  })
  
  if (result === null) {
    // File was locked, skip processing
    return
  }
  
  if (result.success) {
    const { summary } = result
    const hasChanges = summary.filesAdded + summary.filesModified + summary.filesDeleted > 0
    
    if (hasChanges) {
      console.log(`🎉 Intl processing complete with changes:`)
      console.log(`   📁 Files: +${summary.filesAdded} ~${summary.filesModified} -${summary.filesDeleted}`)
      console.log(`   🔑 Keys: +${summary.keysAdded} ~${summary.keysModified} -${summary.keysDeleted}`)
      console.log(`   🌍 Coverage: ${summary.translationCoverage.coveragePercentage}% (${summary.translationCoverage.incompleteKeys} incomplete keys)`)
      console.log(`   ⏱️  Time: ${summary.processingTimeMs}ms`)
    } else {
      const coverageInfo = summary.translationCoverage.incompleteKeys > 0 
        ? ` - ${summary.translationCoverage.coveragePercentage}% coverage (${summary.translationCoverage.incompleteKeys} incomplete keys)`
        : ' - 100% coverage ✅'
      console.log(`✅ Intl processing complete - no changes detected (${summary.processingTimeMs}ms)${coverageInfo}`)
    }
  } else {
    console.error(`💥 Intl processing failed: ${result.error}`)
  }
}