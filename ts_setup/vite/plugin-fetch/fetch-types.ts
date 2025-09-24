
// ============================================================================
// TYPES
// ============================================================================

export interface FetchConfig {
  fetchDirectory: string
  fetchTreeDirectory: string
  fetchTreeGenFile: string
  routeFilePrefix?: string
  routeFileIgnorePrefix?: string
  routeFileIgnorePattern?: string
  moduleName: string;
}

export interface SourceFile {
  fileName: string
  relativePath: string
  dirent: any // From readdir
}

export interface FetchFile {
  path: string
  name: string
  relativePath: string
  isDirectory: boolean
  lastModified: Date
}

export interface GeneratedTree {
  content: string
  fileName: string
  sourceFiles: string[]
  generatedAt: Date
}

export interface FetchDiff {
  hasChanges: boolean
  addedFiles: string[]
  modifiedFiles: string[]
  deletedFiles: string[]
  unchangedFiles: string[]
  previousContent?: string
  newContent: string
  sourceFilesChanged: string[]
}

export interface FetchSummary {
  totalSourceFiles: number
  processedFiles: number
  skippedFiles: number
  generatedTreeSize: number
  processingTimeMs: number
  lastGenerated: Date
}