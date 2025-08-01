
import { IntlFileDiff, DiffSummary, IntlKeyChange } from './intl-types'
// ============================================================================
// DIFF REPORTER COMMAND
// ============================================================================

export declare namespace Command_ReportIntlDiff {
  export interface Input {
    diffs: IntlFileDiff[]
    summary: DiffSummary
    verbose?: boolean
  }
  
  export interface Result {
    reportLines: string[]
    success: boolean
  }
}

export class Command_ReportIntlDiff {
  execute(input: Command_ReportIntlDiff.Input): Command_ReportIntlDiff.Result {
    const { diffs, summary, verbose = false } = input
    const lines: string[] = []
    
    // Header
    lines.push('')
    lines.push('🎨 INTL CHANGES REPORT')
    lines.push('═'.repeat(60))
    
    // Summary
    lines.push(`📊 SUMMARY (${summary.processingTimeMs}ms)`)
    lines.push('─'.repeat(30))
    lines.push(`📁 Files: ${summary.totalFiles} total`)
    lines.push(`   ├─ 🆕 Added: ${summary.filesAdded}`)
    lines.push(`   ├─ 📝 Modified: ${summary.filesModified}`)
    lines.push(`   ├─ 🗑️  Deleted: ${summary.filesDeleted}`)
    lines.push(`   └─ ✅ Unchanged: ${summary.filesUnchanged}`)
    lines.push('')
    lines.push(`🔑 Keys: ${summary.totalKeys} total`)
    lines.push(`   ├─ ➕ Added: ${summary.keysAdded}`)
    lines.push(`   ├─ ✏️  Modified: ${summary.keysModified}`)
    lines.push(`   └─ ➖ Deleted: ${summary.keysDeleted}`)
    lines.push('')
    lines.push(`🌍 Locales: ${summary.localesAffected.join(', ')} (${summary.localesAffected.length} total)`)
    
    // Translation Coverage
    const coverage = summary.translationCoverage
    const coverageIcon = coverage.coveragePercentage === 100 ? '✅' : 
                        coverage.coveragePercentage >= 90 ? '🟡' : '🔴'
    
    lines.push('')
    lines.push(`${coverageIcon} TRANSLATION COVERAGE: ${coverage.coveragePercentage}%`)
    lines.push('─'.repeat(30))
    lines.push(`📈 ${coverage.actualTranslations}/${coverage.totalExpectedTranslations} translations complete`)
    
    if (coverage.incompleteKeys > 0) {
      lines.push(`⚠️  ${coverage.incompleteKeys} keys missing translations`)
      
      // Show top missing translations
      const topMissing = summary.missingTranslations
        .slice(0, verbose ? 20 : 5)
        .sort((a, b) => b.missingInLocales.length - a.missingInLocales.length)
      
      if (topMissing.length > 0) {
        lines.push('')
        lines.push(`🚨 MISSING TRANSLATIONS (showing top ${topMissing.length}):`)
        
        for (const missing of topMissing) {
          const missingCount = missing.missingInLocales.length
          const totalLocales = missing.missingInLocales.length + missing.availableInLocales.length
          const missingPercent = Math.round((missingCount / totalLocales) * 100)
          
          lines.push(`   🔸 "${missing.key}" missing in ${missingCount}/${totalLocales} locales (${missingPercent}%)`)
          
          if (verbose) {
            lines.push(`      Missing: ${missing.missingInLocales.join(', ')}`)
            lines.push(`      Available: ${missing.availableInLocales.join(', ')}`)
          } else {
            lines.push(`      Missing: ${missing.missingInLocales.join(', ')}`)
          }
        }
        
        if (summary.missingTranslations.length > topMissing.length) {
          const remaining = summary.missingTranslations.length - topMissing.length
          lines.push(`   ... and ${remaining} more incomplete keys`)
        }
      }
    } else {
      lines.push(`🎉 All translations complete!`)
    }
    
    // Only show details if there are changes
    const changedDiffs = diffs.filter(d => d.changeType !== 'unchanged')
    
    if (changedDiffs.length === 0) {
      lines.push('')
      lines.push('🎉 No content changes detected! All files are up to date.')
    } else {
      lines.push('')
      lines.push('📋 DETAILED CHANGES')
      lines.push('─'.repeat(30))
      
      for (const diff of changedDiffs) {
        this.reportFileDiff(diff, lines, verbose)
      }
    }
    
    lines.push('')
    lines.push('═'.repeat(60))
    
    // Print all lines
    lines.forEach(line => console.log(line))
    
    return { reportLines: lines, success: true }
  }
  
  private reportFileDiff(diff: IntlFileDiff, lines: string[], verbose: boolean): void {
    const icon = this.getChangeIcon(diff.changeType)
    lines.push(`${icon} ${diff.fileName} (${diff.keyChanges.length} key changes)`)
    
    if (verbose || diff.keyChanges.length <= 10) {
      // Show all key changes if verbose or if there are few changes
      for (const keyChange of diff.keyChanges) {
        const keyIcon = this.getChangeIcon(keyChange.changeType)
        lines.push(`   ${keyIcon} ${keyChange.locale}.${keyChange.key}`)
        
        if (keyChange.changeType === 'modified' && verbose) {
          lines.push(`      Old: "${keyChange.previousValue}"`)
          lines.push(`      New: "${keyChange.newValue}"`)
        } else if (keyChange.changeType === 'added') {
          lines.push(`      Value: "${keyChange.newValue}"`)
        } else if (keyChange.changeType === 'deleted' && verbose) {
          lines.push(`      Deleted: "${keyChange.previousValue}"`)
        }
      }
    } else {
      // Summarize key changes if there are many
      const keysByType = this.groupKeyChangesByType(diff.keyChanges)
      for (const [changeType, keys] of keysByType.entries()) {
        const keyIcon = this.getChangeIcon(changeType)
        lines.push(`   ${keyIcon} ${keys.length} keys ${changeType}`)
        if (verbose) {
          keys.slice(0, 5).forEach(key => {
            lines.push(`      • ${key.locale}.${key.key}`)
          })
          if (keys.length > 5) {
            lines.push(`      ... and ${keys.length - 5} more`)
          }
        }
      }
    }
    
    lines.push('')
  }
  
  private getChangeIcon(changeType: string): string {
    switch (changeType) {
      case 'added': return '🆕'
      case 'modified': return '📝'
      case 'deleted': return '🗑️'
      case 'unchanged': return '✅'
      default: return '❓'
    }
  }
  
  private groupKeyChangesByType(keyChanges: IntlKeyChange[]): Map<string, IntlKeyChange[]> {
    const groups = new Map<string, IntlKeyChange[]>()
    
    for (const keyChange of keyChanges) {
      const existing = groups.get(keyChange.changeType) || []
      existing.push(keyChange)
      groups.set(keyChange.changeType, existing)
    }
    
    return groups
  }
}