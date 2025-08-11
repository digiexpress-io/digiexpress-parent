
// ============================================================================
// DIFF CALCULATION COMMAND
// ============================================================================

import { DiffSummary, IntlFile, IntlFileDiff, IntlKeyChange, MissingTranslation, TranslationCoverage } from "./intl-types"

export declare namespace Command_CalculateIntlDiff {
  export interface Input {
    newFiles: IntlFile[]
    existingFiles: Map<string, string>
  }
  
  export interface Result {
    diffs: IntlFileDiff[]
    summary: DiffSummary
    success: boolean
    error?: string
  }
}

export class Command_CalculateIntlDiff {
  execute(input: Command_CalculateIntlDiff.Input): Command_CalculateIntlDiff.Result {
    const startTime = Date.now()
    
    try {
      console.log(`🔍 Calculating diffs for ${input.newFiles.length} files`)
      
      const diffs: IntlFileDiff[] = []
      const allNewFileNames = new Set(input.newFiles.map(f => f.fileName))
      const allExistingFileNames = new Set(input.existingFiles.keys())
      const localesAffected = new Set<string>()
      
      let totalKeys = 0
      let keysAdded = 0
      let keysModified = 0
      let keysDeleted = 0
      
      // Process new/modified files
      for (const newFile of input.newFiles) {
        const existingContent = input.existingFiles.get(newFile.fileName)
        const isNewFile = !existingContent
        const locale = this.extractLocaleFromFileName(newFile.fileName)
        localesAffected.add(locale)
        
        if (isNewFile) {
          // Completely new file
          const keyChanges = this.extractKeysFromContent(newFile.content, 'added', newFile.fileName)
          diffs.push({
            fileName: newFile.fileName,
            changeType: 'added',
            keyChanges,
            newContent: newFile.content
          })
          
          keysAdded += keyChanges.length
          totalKeys += keyChanges.length
        } else {
          // Compare existing file
          const fileDiff = this.compareFileContents(
            newFile.fileName, 
            existingContent, 
            newFile.content
          )
          
          diffs.push(fileDiff)
          
          fileDiff.keyChanges.forEach(kc => {
            totalKeys++
            
            switch (kc.changeType) {
              case 'added': keysAdded++; break
              case 'modified': keysModified++; break
              case 'deleted': keysDeleted++; break
            }
          })
        }
      }
      
      // Process deleted files
      for (const existingFileName of allExistingFileNames) {
        if (!allNewFileNames.has(existingFileName)) {
          const existingContent = input.existingFiles.get(existingFileName)!
          const keyChanges = this.extractKeysFromContent(existingContent, 'deleted', existingFileName)
          const locale = this.extractLocaleFromFileName(existingFileName)
          localesAffected.add(locale)
          
          diffs.push({
            fileName: existingFileName,
            changeType: 'deleted',
            keyChanges,
            previousContent: existingContent
          })
          
          keysDeleted += keyChanges.length
          totalKeys += keyChanges.length
        }
      }
      
      // Calculate missing translations
      const { missingTranslations, translationCoverage } = this.calculateMissingTranslations(input.newFiles)
      
      const summary: DiffSummary = {
        totalFiles: Math.max(allNewFileNames.size, allExistingFileNames.size),
        filesAdded: diffs.filter(d => d.changeType === 'added').length,
        filesModified: diffs.filter(d => d.changeType === 'modified').length,
        filesDeleted: diffs.filter(d => d.changeType === 'deleted').length,
        filesUnchanged: diffs.filter(d => d.changeType === 'unchanged').length,
        totalKeys,
        keysAdded,
        keysModified,
        keysDeleted,
        localesAffected: Array.from(localesAffected).sort(),
        missingTranslations,
        translationCoverage,
        processingTimeMs: Date.now() - startTime
      }
      
      console.log(`✅ Diff calculation completed in ${summary.processingTimeMs}ms`)
      
      return { diffs, summary, success: true }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error)
      console.error(`❌ Failed to calculate diffs:`, errorMessage)
      
      return {
        diffs: [],
        summary: this.createEmptySummary(Date.now() - startTime),
        success: false,
        error: errorMessage
      }
    }
  }
  
  private calculateMissingTranslations(newFiles: IntlFile[]): {
    missingTranslations: MissingTranslation[]
    translationCoverage: TranslationCoverage
  } {
    // Build a map of locale -> keys
    const localeKeys = new Map<string, Set<string>>()
    const allKeys = new Set<string>()
    const allLocales = new Set<string>()
    
    for (const file of newFiles) {
      const locale = this.extractLocaleFromFileName(file.fileName)
      if (locale === 'index') continue // Skip index file
      
      allLocales.add(locale)
      const keys = this.parseIntlKeys(file.content, file.fileName)
      localeKeys.set(locale, new Set(keys.keys()))
      
      // Collect all unique keys across all locales
      keys.forEach((_, key) => allKeys.add(key))
    }
    
    const missingTranslations: MissingTranslation[] = []
    let totalActualTranslations = 0
    
    // Check each key across all locales
    for (const key of allKeys) {
      const missingInLocales: string[] = []
      const availableInLocales: string[] = []
      
      for (const locale of allLocales) {
        const localeKeySet = localeKeys.get(locale)
        if (localeKeySet?.has(key)) {
          availableInLocales.push(locale)
          totalActualTranslations++
        } else {
          missingInLocales.push(locale)
        }
      }
      
      if (missingInLocales.length > 0) {
        missingTranslations.push({
          key,
          missingInLocales: missingInLocales.sort(),
          availableInLocales: availableInLocales.sort()
        })
      }
    }
    
    const totalKeys = allKeys.size
    const totalExpectedTranslations = totalKeys * allLocales.size
    const coveragePercentage = totalExpectedTranslations > 0 
      ? Math.round((totalActualTranslations / totalExpectedTranslations) * 100)
      : 100
    const incompleteKeys = missingTranslations.length
    
    const translationCoverage: TranslationCoverage = {
      totalKeys,
      totalExpectedTranslations,
      actualTranslations: totalActualTranslations,
      coveragePercentage,
      incompleteKeys
    }
    
    return { missingTranslations, translationCoverage }
  }
  
  private createEmptySummary(processingTimeMs: number): DiffSummary {
    return {
      totalFiles: 0, filesAdded: 0, filesModified: 0, filesDeleted: 0, filesUnchanged: 0,
      totalKeys: 0, keysAdded: 0, keysModified: 0, keysDeleted: 0,
      localesAffected: [],
      missingTranslations: [],
      translationCoverage: {
        totalKeys: 0, totalExpectedTranslations: 0, actualTranslations: 0,
        coveragePercentage: 100, incompleteKeys: 0
      },
      processingTimeMs
    }
  }
  
  private compareFileContents(fileName: string, oldContent: string, newContent: string): IntlFileDiff {
    if (oldContent === newContent) {
      return {
        fileName,
        changeType: 'unchanged',
        keyChanges: [],
        previousContent: oldContent,
        newContent
      }
    }
    
    const oldKeys = this.parseIntlKeys(oldContent, fileName)
    const newKeys = this.parseIntlKeys(newContent, fileName)
    const keyChanges: IntlKeyChange[] = []
    
    const allKeys = new Set([...oldKeys.keys(), ...newKeys.keys()])
    
    for (const key of allKeys) {
      const oldValue = oldKeys.get(key)
      const newValue = newKeys.get(key)
      const locale = this.extractLocaleFromFileName(fileName)
      
      if (!oldValue && newValue) {
        // Key added
        keyChanges.push({
          key,
          changeType: 'added',
          newValue,
          locale
        })
      } else if (oldValue && !newValue) {
        // Key deleted
        keyChanges.push({
          key,
          changeType: 'deleted',
          previousValue: oldValue,
          locale
        })
      } else if (oldValue && newValue && oldValue !== newValue) {
        // Key modified
        keyChanges.push({
          key,
          changeType: 'modified',
          previousValue: oldValue,
          newValue,
          locale
        })
      }
    }
    
    return {
      fileName,
      changeType: keyChanges.length > 0 ? 'modified' : 'unchanged',
      keyChanges,
      previousContent: oldContent,
      newContent
    }
  }
  
  private extractKeysFromContent(content: string, changeType: 'added' | 'deleted', fileName: string): IntlKeyChange[] {
    const keys = this.parseIntlKeys(content, fileName)
    const locale = this.extractLocaleFromFileName(fileName)
    
    return Array.from(keys.entries()).map(([key, value]) => ({
      key,
      changeType,
      ...(changeType === 'added' ? { newValue: value } : { previousValue: value }),
      locale
    }))
  }
  
  private parseIntlKeys(content: string, fileName: string): Map<string, string> {
    const keys = new Map<string, string>()
    
    // Parse TypeScript object notation: 'key': 'value'
    const keyValueRegex = /'([^']+)':\s*'([^']*)'/g
    let match
    
    while ((match = keyValueRegex.exec(content)) !== null) {
      const [, key, value] = match
      keys.set(key, value)
    }
    
    return keys
  }
  
  private extractLocaleFromFileName(fileName: string): string {
    return fileName.replace('.ts', '')
  }
}