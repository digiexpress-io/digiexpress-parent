
// ============================================================================
// REPORT FETCH CHANGES COMMAND
// ============================================================================

import { FetchConfig, FetchDiff, FetchSummary } from "./fetch-types"

export declare namespace Command_ReportFetchChanges {
  export interface Input {
    diff: FetchDiff
    summary: FetchSummary
    config: FetchConfig
    verbose?: boolean
  }
  
  export interface Result {
    reportLines: string[]
    success: boolean
  }
}

export class Command_ReportFetchChanges {
  execute(input: Command_ReportFetchChanges.Input): Command_ReportFetchChanges.Result {
    const { diff, summary, config, verbose = false } = input
    const lines: string[] = []
    
    // Header
    lines.push('')
    lines.push('🌳 FETCH TREE GENERATION REPORT')
    lines.push('═'.repeat(60))
    
    // Summary
    lines.push(`📊 SUMMARY (${summary.processingTimeMs}ms)`)
    lines.push('─'.repeat(30))
    lines.push(`📂 Source directory: ${config.fetchDirectory}`)
    lines.push(`📄 Generated file: ${config.fetchTreeGenFile}`)
    lines.push(`📊 Files processed: ${summary.processedFiles}/${summary.totalSourceFiles}`)
    lines.push(`📏 Generated size: ${summary.generatedTreeSize.toLocaleString()} characters`)
    lines.push(`⏰ Generated at: ${summary.lastGenerated.toLocaleString()}`)
    
    // Changes
    lines.push('')
    if (diff.hasChanges) {
      lines.push('📝 CHANGES DETECTED')
      lines.push('─'.repeat(30))
      
      if (diff.addedFiles.length > 0) {
        lines.push(`🆕 New tree file created`)
      }
      
      if (diff.modifiedFiles.length > 0) {
        lines.push(`✏️  Tree file updated`)
      }
      
      if (verbose && diff.previousContent) {
        const sizeDiff = diff.newContent.length - diff.previousContent.length
        const sizeDiffStr = sizeDiff > 0 ? `+${sizeDiff}` : sizeDiff.toString()
        lines.push(`📏 Size change: ${sizeDiffStr} characters`)
      }
      
      lines.push(`🔄 Source files: ${diff.sourceFilesChanged.length}`)
      
      if (verbose) {
        lines.push('')
        lines.push('📋 SOURCE FILES:')
        diff.sourceFilesChanged.slice(0, 10).forEach(file => {
          lines.push(`   📄 ${file}`)
        })
        if (diff.sourceFilesChanged.length > 10) {
          lines.push(`   ... and ${diff.sourceFilesChanged.length - 10} more`)
        }
      }
      
    } else {
      lines.push('✅ NO CHANGES')
      lines.push('─'.repeat(30))
      lines.push('🎉 Generated tree is up to date!')
    }
    
    lines.push('')
    lines.push('═'.repeat(60))
    
    // Print all lines
    lines.forEach(line => console.log(line))
    
    return { reportLines: lines, success: true }
  }
}