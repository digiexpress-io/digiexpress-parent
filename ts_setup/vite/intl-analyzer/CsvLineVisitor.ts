import { Line, ParseStats } from "./intl-types";

export class CsvLineVisitor {
  private readonly _lines: Line[] = [];
  private readonly _visited = new Set<string>();
  private readonly _stats: ParseStats;
  private _currentLine = 0;
  private _header: { values: string[], lineNumber: number, type: 'header', comments: string[]};
  private _pendingComments: string[] = []; // Buffer for comments waiting to be attached

  constructor() {
    const header: Line = {
      lineNumber: 0,
      type: 'header',
      values: [],
      comments: []
    };
    this._header = header;
    this._lines.push(header)
    this._stats = {
      totalLines: 0,
      processedLines: 0,
      skippedLines: 0,
      emptyLines: 0,
      commentLines: 0,
      duplicateKeys: 0,
      localesFound: new Set(),
      keysProcessed: 0
    };
  }

  visitLine(object: Record<string, string>): void {
    this._currentLine++;
    this._stats.totalLines++;
    const intlKey = this.extractIntlKey(object);
    const isComment = intlKey && this.isComment(intlKey);
    const isEmptyLine = Object.entries(object).length === 0 || !intlKey?.trim();

    // Handle empty lines - they clear pending comments
    if (isEmptyLine) {
      this._stats.emptyLines++;
      this._stats.skippedLines++;
      //this._lines.push({ lineNumber: this._currentLine, type: 'empty' });
      //this._pendingComments = []; // Clear pending comments on empty lines
      return;
    }

    this._header.values = Array.from(new Set([...this._header.values, ...Object.keys(object)]));

    // Handle comment lines - accumulate them in pending buffer
    if (intlKey && isComment) {
      this._stats.commentLines++;
      this._stats.skippedLines++;
      this._pendingComments.push(intlKey); // Add to pending comments instead of creating line
      return;
    }

    // Handle translation lines
    if (!intlKey) {
      this._stats.skippedLines++;
      this._lines.push({ 
        lineNumber: this._currentLine, 
        type: 'error', 
        error: `❌ Line ${this._currentLine}: Missing 'id' column. Available columns: [${Object.keys(object).join(', ')}]`,
        values: {...object},
        comments: [...this._pendingComments] // Attach pending comments
      });
      this._pendingComments = []; // Clear after attaching
      return;
    }

    const locales = this.extractLocales(object);
    
    if (locales.length === 0) {
      this._stats.skippedLines++;
      this._lines.push({ 
        lineNumber: this._currentLine, 
        type: 'error', 
        error: `❌ Line ${this._currentLine}: No valid locale columns found for key "${intlKey}"`,
        values: {...object},
        comments: [...this._pendingComments] // Attach pending comments
      });
      this._pendingComments = []; // Clear after attaching
      return;
    }

    // Track new locales
    locales.forEach(locale => this._stats.localesFound.add(locale));

    // Check for duplicates
    if (this._visited.has(intlKey)) {
      this._stats.duplicateKeys++;
      this._lines.push({ 
        lineNumber: this._currentLine, 
        type: 'error', 
        error: `❌ Duplicate key "${intlKey}" found at line ${this._currentLine}`,
        values: {...object},
        comments: [...this._pendingComments] // Attach pending comments
      });
      this._pendingComments = []; // Clear after attaching
      return;
    }

    this._visited.add(intlKey);

    // Create translation line with processed values
    const translationLine: Extract<Line, { type: 'translations' }> = {
      lineNumber: this._currentLine,
      type: 'translations',
      id: intlKey,
      info: Object.entries(object).find(([key]) => key.toLowerCase() === 'info')?.[1] ?? '',
      values: {},
      comments: [...this._pendingComments] // Attach pending comments
    };

    // Add locale values
    for (const locale of locales) {
      const intlValue = this.extractIntlValue(locale, object);
      translationLine.values[locale] = intlValue;
    }

    this._lines.push(translationLine);
    this._stats.processedLines++;
    this._stats.keysProcessed++;
    this._pendingComments = []; // Clear after attaching
  }

  private isComment(keyOrValue: string): boolean {
    return keyOrValue.trim().startsWith('//');
  }

  private extractLocales(object: Record<string, string>): string[] {
    return Object.keys(object)
      .map(key => key.toLowerCase().trim())
      .filter(key => key !== 'id')
      .filter(key => key !== 'info') // Also filter out info column
      .filter(key => {
        const value = object[key];
        return value && value.trim() && !this.isComment(value);
      })
      .filter(key => key.length === 2); // Standard ISO 639-1 language codes
  }

  private extractIntlKey(object: Record<string, string>): string | undefined {
    const idEntry = Object.entries(object).find(([key]) => 
      key.toLowerCase().trim() === 'id'
    );
    return idEntry?.[1]?.trim();
  }

  private extractIntlValue(locale: string, object: Record<string, string>): string {
    const rawValue = object[locale] || '';
    // Handle escaped quotes properly
    return rawValue
      .replace(/\\'/g, "'")  // Convert \' to '
      .replace(/'/g, "\\'")  // Escape remaining single quotes
      .trim();
  }

  close(): { lines: Line[]; parseStats: ParseStats } {
    // Attach any remaining pending comments to the header
    if (this._pendingComments.length > 0) {
      this._header.comments.push(...this._pendingComments);
    }
    
    this.logFinalStats();
    return {
      lines: this._lines,
      parseStats: this._stats
    };
  }

  private logFinalStats(): void {
    const { _stats: stats } = this;
    
    console.log('\n🎯 CSV Line Parsing Complete!');
    console.log('━'.repeat(50));
    console.log(`📊 Total lines processed: ${stats.totalLines}`);
    console.log(`✅ Translation lines: ${stats.processedLines}`);
    console.log(`⏭️  Other lines: ${stats.skippedLines}`);
    console.log(`   ├─ Empty lines: ${stats.emptyLines}`);
    console.log(`   ├─ Comment lines: ${stats.commentLines}`);
    console.log(`   └─ Other skipped: ${stats.skippedLines - stats.emptyLines - stats.commentLines}`);
    console.log(`🔑 Translation keys processed: ${stats.keysProcessed}`);
    console.log(`🌍 Locales found: ${Array.from(stats.localesFound).sort().join(', ')} (${stats.localesFound.size} total)`);
    
    if (stats.duplicateKeys > 0) {
      console.log(`🔄 Duplicate keys found: ${stats.duplicateKeys}`);
    }
    
    const successRate = stats.totalLines > 0 ? ((stats.processedLines / stats.totalLines) * 100).toFixed(1) : '0.0';
    console.log(`📈 Translation success rate: ${successRate}%`);
    console.log('━'.repeat(50));
  }
}