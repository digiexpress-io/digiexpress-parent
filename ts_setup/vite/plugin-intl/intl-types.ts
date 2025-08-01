export interface IntlFile {
  fileName: string
  content: string
}

// ============================================================================
// DIFF TYPES
// ============================================================================

export interface IntlFileDiff {
  fileName: string
  changeType: 'added' | 'modified' | 'deleted' | 'unchanged'
  keyChanges: IntlKeyChange[]
  previousContent?: string
  newContent?: string
}

export interface IntlKeyChange {
  key: string
  changeType: 'added' | 'modified' | 'deleted'
  previousValue?: string
  newValue?: string
  locale: string
}

export interface DiffSummary {
  totalFiles: number
  filesAdded: number
  filesModified: number
  filesDeleted: number
  filesUnchanged: number
  totalKeys: number
  keysAdded: number
  keysModified: number
  keysDeleted: number
  localesAffected: string[]
  missingTranslations: MissingTranslation[]
  translationCoverage: TranslationCoverage
  processingTimeMs: number
}

export interface MissingTranslation {
  key: string
  missingInLocales: string[]
  availableInLocales: string[]
}

export interface TranslationCoverage {
  totalKeys: number
  totalExpectedTranslations: number // totalKeys * locales.length
  actualTranslations: number
  coveragePercentage: number
  incompleteKeys: number // keys that don't have all locale translations
}