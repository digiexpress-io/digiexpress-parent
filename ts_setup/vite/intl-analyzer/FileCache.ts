import { glob } from 'glob';
import * as fs from 'node:fs/promises';
import * as path from 'node:path';

export interface CachedFile {
  path: string;
  content: string;
}

export class FileCache {
  private cache = new Map<string, CachedFile[]>();
  private stats = {
    hits: 0,
    misses: 0,
    filesRead: 0,
    totalReadTime: 0
  };

  async readFiles(modulePath: string): Promise<CachedFile[]> {
    // Check cache first
    if (this.cache.has(modulePath)) {
      this.stats.hits++;
      console.log(`📚 Cache HIT for module: ${modulePath} (${this.cache.get(modulePath)!.length} files)`);
      return this.cache.get(modulePath)!;
    }

    // Cache miss - read files from disk
    this.stats.misses++;
    console.log(`📁 Cache MISS - Reading files from: ${modulePath}`);
    
    const startTime = Date.now();
    
    try {
      // Find all TypeScript files in the module
      const filePaths = await this.findTypeScriptFiles(modulePath);
      
      if (filePaths.length === 0) {
        console.log(`📭 No TypeScript files found in: ${modulePath}`);
        this.cache.set(modulePath, []);
        return [];
      }

      console.log(`🔍 Found ${filePaths.length} TypeScript files, reading in parallel...`);

      // Read all files in parallel
      const filePromises = filePaths.map(async (filePath): Promise<CachedFile | null> => {
        try {
          const content = await fs.readFile(filePath, 'utf-8');
          return { path: filePath, content };
        } catch (error) {
          console.warn(`⚠️  Failed to read file ${filePath}:`, error);
          return null; // Return null for failed reads
        }
      });

      // Wait for all files to be read
      const results = await Promise.all(filePromises);
      
      // Filter out failed reads and collect successful ones
      const cachedFiles: CachedFile[] = results.filter((file): file is CachedFile => file !== null);
      
      // Update stats
      this.stats.filesRead += cachedFiles.length;
      const readTime = Date.now() - startTime;
      this.stats.totalReadTime += readTime;

      // Cache the results
      this.cache.set(modulePath, cachedFiles);
      
      const failedCount = filePaths.length - cachedFiles.length;
      console.log(`✅ Read ${cachedFiles.length} files in ${readTime}ms${failedCount > 0 ? ` (${failedCount} failed)` : ''}`);
      
      return cachedFiles;

    } catch (error) {
      console.error(`💥 Error reading module ${modulePath}:`, error);
      // Cache empty result to avoid repeated failures
      this.cache.set(modulePath, []);
      return [];
    }
  }

  private async findTypeScriptFiles(modulePath: string): Promise<string[]> {
    try {
      const pattern = path.join(modulePath, '**/*.{ts,tsx}');
      const files = await glob(pattern, {
        ignore: [
          '**/node_modules/**',
          '**/dist/**',
          '**/build/**',
          '**/coverage/**',
          '**/.next/**',
          '**/.nuxt/**',
          '**/*-intl/**',
          '**/out/**',
          '**/*.d.ts'
        ],
        absolute: true
      });
      
      return files;
    } catch (error) {
      console.error(`💥 Error finding TypeScript files in ${modulePath}:`, error);
      return [];
    }
  }

  // Utility methods
  clearCache(): void {
    this.cache.clear();
    console.log('🗑️  File cache cleared');
  }

  getCacheStats() {
    const avgReadTime = this.stats.totalReadTime > 0 ? 
      Math.round(this.stats.totalReadTime / this.stats.misses) : 0;
    
    return {
      ...this.stats,
      cachedModules: this.cache.size,
      avgReadTimePerModule: avgReadTime
    };
  }
}