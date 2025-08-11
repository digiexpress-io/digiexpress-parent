export class FileLockManager {
  private static readonly locks = new Map<string, boolean>()
  
  static isLocked(filePath: string): boolean {
    return this.locks.get(filePath) ?? false
  }
  
  static lock(filePath: string): boolean {
    if (this.isLocked(filePath)) {
      return false // Already locked
    }
    this.locks.set(filePath, true)
    return true // Successfully locked
  }
  
  static unlock(filePath: string): void {
    this.locks.delete(filePath)
  }
  
  static async withLock<T>(
    filePath: string, 
    operation: () => Promise<T>
  ): Promise<T | null> {
    if (!this.lock(filePath)) {
      console.log(`⏳ Skipping operation for ${filePath} - already in progress`)
      return null
    }
    
    try {
      return await operation()
    } finally {
      this.unlock(filePath)
    }
  }
}