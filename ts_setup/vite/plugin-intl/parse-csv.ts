import csv from 'csv-parser'
import { createReadStream } from 'node:fs'
import { basename } from 'node:path'
import { IntlFile } from './intl-types'



interface ParseStats {
  totalLines: number
  processedLines: number
  skippedLines: number
  emptyLines: number
  commentLines: number
  duplicateKeys: number
  localesFound: Set<string>
  keysProcessed: number
}

class CsvReadVisitor {
  private readonly _ignoreErrors: boolean
  private readonly _locales: Record<string, string[]> = {}
  private readonly _visited = new Set<string>()
  private readonly _stats: ParseStats
  private _currentLine = 0

  constructor(ignoreErrors: boolean) {
    this._ignoreErrors = ignoreErrors
    this._stats = {
      totalLines: 0,
      processedLines: 0,
      skippedLines: 0,
      emptyLines: 0,
      commentLines: 0,
      duplicateKeys: 0,
      localesFound: new Set(),
      keysProcessed: 0
    }
  }

  visitLine(object: Record<string, string>): void {
    this._currentLine++
    this._stats.totalLines++

    const intlKey = this.extractIntlKey(object)
    const isEmptyLine = Object.entries(object).length === 0 || !intlKey?.trim()

    if (isEmptyLine) {
      this._stats.emptyLines++
      this._stats.skippedLines++
      console.log(`📋 Line ${this._currentLine}: Empty line skipped`)
      return
    }

    if (!intlKey) {
      this._stats.skippedLines++
      const availableColumns = Object.keys(object).join(', ')
      console.error(`❌ Line ${this._currentLine}: Missing 'id' column. Available columns: [${availableColumns}]`)
      if (!this._ignoreErrors) {
        throw new Error(`Missing required 'id' column at line ${this._currentLine}`)
      }
      return
    }

    if (this.isComment(intlKey)) {
      this._stats.commentLines++
      this._stats.skippedLines++
      console.log(`💬 Line ${this._currentLine}: Comment line skipped - "${intlKey}"`)
      return
    }

    const locales = this.extractLocales(object)
    
    if (locales.length === 0) {
      this._stats.skippedLines++
      console.warn(`⚠️  Line ${this._currentLine}: No valid locale columns found for key "${intlKey}"`)
      return
    }

    // Track new locales
    locales.forEach(locale => this._stats.localesFound.add(locale))

    let processedForCurrentKey = 0
    for (const locale of locales) {
      const intlValue = this.extractIntlValue(locale, object)
      if (this.processIntlEntry({ locale, intlKey, intlValue })) {
        processedForCurrentKey++
      }
    }

    if (processedForCurrentKey > 0) {
      this._stats.processedLines++
      this._stats.keysProcessed++
      console.log(`✅ Line ${this._currentLine}: Processed key "${intlKey}" for ${processedForCurrentKey} locale(s)`)
    }
  }

  private isComment(keyOrValue: string): boolean {
    return keyOrValue.trim().startsWith('//')
  }

  private extractLocales(object: Record<string, string>): string[] {
    return Object.keys(object)
      .map(key => key.toLowerCase().trim())
      .filter(key => key !== 'id')
      .filter(key => {
        const value = object[key]
        return value && value.trim() && !this.isComment(value)
      })
      .filter(key => key.length === 2) // Standard ISO 639-1 language codes
  }

  private extractIntlKey(object: Record<string, string>): string | undefined {
    const idEntry = Object.entries(object).find(([key]) => 
      key.toLowerCase().trim() === 'id'
    )
    return idEntry?.[1]?.trim()
  }

  private extractIntlValue(locale: string, object: Record<string, string>): string {
    const rawValue = object[locale] || ''
    // Handle escaped quotes properly
    return rawValue
      .replace(/\\'/g, "'")  // Convert \' to '
      .replace(/'/g, "\\'")  // Escape remaining single quotes
      .trim()
  }

  private processIntlEntry(props: { locale: string; intlKey: string; intlValue: string }): boolean {
    const { locale, intlKey, intlValue } = props
    
    if (!this._locales[locale]) {
      this._locales[locale] = []
      console.log(`🌍 New locale discovered: "${locale}"`)
    }

    const uniqueKey = `${locale}.${intlKey}`
    
    if (this._visited.has(uniqueKey)) {
      this._stats.duplicateKeys++
      console.error(`🔄 Duplicate key detected: "${uniqueKey}" at line ${this._currentLine}`)
      
      if (!this._ignoreErrors) {
        throw new Error(`Duplicate key "${uniqueKey}" found at line ${this._currentLine}`)
      }
      return false
    }

    this._visited.add(uniqueKey)
    this._locales[locale].push(`'${intlKey}': '${intlValue}'`)
    return true
  }

  private generateLocaleFile(locale: string): IntlFile {
    const entries = this._locales[locale]
    const formattedEntries = entries.map(entry => `  ${entry}`).join(',\n')
    
    const content = `export const ${locale} = {\n${formattedEntries}\n}`
    
    return {
      fileName: `${locale}.ts`,
      content
    }
  }

  private generateIndexFile(): IntlFile {
    const locales = Object.keys(this._locales).sort()
    const imports = locales
      .map(locale => `import { ${locale} } from './${locale}'`)
      .join('\n')
    
    const exportStatement = `export const messages = { ${locales.join(', ')} }`
    const content = `${imports}\n\n${exportStatement}`
    
    return { 
      fileName: 'index.ts', 
      content 
    }
  }

  close(): IntlFile[] {
    this.logFinalStats()
    
    const localeFiles = Object.keys(this._locales)
      .sort()
      .map(locale => this.generateLocaleFile(locale))
    
    return [this.generateIndexFile(), ...localeFiles]
  }

  private logFinalStats(): void {
    const { _stats: stats } = this
    
    console.log('\n🎯 CSV Processing Complete!')
    console.log('━'.repeat(50))
    console.log(`📊 Total lines processed: ${stats.totalLines}`)
    console.log(`✅ Successfully processed: ${stats.processedLines}`)
    console.log(`⏭️  Skipped lines: ${stats.skippedLines}`)
    console.log(`   ├─ Empty lines: ${stats.emptyLines}`)
    console.log(`   ├─ Comment lines: ${stats.commentLines}`)
    console.log(`   └─ Other skipped: ${stats.skippedLines - stats.emptyLines - stats.commentLines}`)
    console.log(`🔑 Translation keys processed: ${stats.keysProcessed}`)
    console.log(`🌍 Locales found: ${Array.from(stats.localesFound).sort().join(', ')} (${stats.localesFound.size} total)`)
    
    if (stats.duplicateKeys > 0) {
      console.log(`🔄 Duplicate keys found: ${stats.duplicateKeys}`)
    }
    
    const successRate = ((stats.processedLines / stats.totalLines) * 100).toFixed(1)
    console.log(`📈 Success rate: ${successRate}%`)
    console.log('━'.repeat(50))
  }
}

export async function parseCsv(filePath: string, ignoreErrors = false): Promise<IntlFile[]> {
  const fileName = basename(filePath)
  console.log(`🚀 Starting CSV parsing: "${fileName}"`)
  console.log(`⚙️  Ignore errors: ${ignoreErrors ? '✅' : '❌'}`)
  console.log('━'.repeat(50))

  const visitor = new CsvReadVisitor(ignoreErrors)
  
  return new Promise<IntlFile[]>((resolve, reject) => {
    const startTime = Date.now()
    
    createReadStream(filePath)
      .pipe(csv())
      .on('data', (data) => {
        try {
          visitor.visitLine(data)
        } catch (error) {
          reject(error)
        }
      })
      .on('error', (error) => {
        console.error(`💥 CSV parsing failed: ${error.message}`)
        reject(error)
      })
      .on('end', () => {
        const duration = Date.now() - startTime
        console.log(`⏱️  Parsing completed in ${duration}ms`)
        
        try {
          const result = visitor.close()
          console.log(`📦 Generated ${result.length} files (1 index + ${result.length - 1} locale files)`)
          resolve(result)
        } catch (error) {
          reject(error)
        }
      })
  })
}