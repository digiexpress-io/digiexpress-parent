import csv from 'csv-parser';
import { createReadStream } from 'node:fs';


import { CsvLineVisitor } from "./CsvLineVisitor";
import { Line, ParseStats } from "./intl-types";

export declare namespace Command_ParseInputCsv {
  export interface Input {
    csvPath: string;
  }
  export interface Result {
    lines: Line[];
    parseStats: ParseStats;
  }
}


export class Command_ParseInputCsv {
  async execute(input: Command_ParseInputCsv.Input): Promise<Command_ParseInputCsv.Result> {
    console.log(`🚀 Parsing CSV: ${input.csvPath}`);
    
    const result = await this.parseCsvToLines(input.csvPath);
    
    console.log(`✅ Parsed ${result.lines.length} lines`);
    return result;
  }

  private async parseCsvToLines(filePath: string): Promise<{ lines: Line[]; parseStats: ParseStats }> {
    const visitor = new CsvLineVisitor();
    
    return new Promise<{ lines: Line[]; parseStats: ParseStats }>((resolve, reject) => {
      const startTime = Date.now();
      
      createReadStream(filePath)
        .pipe(csv())
        .on('data', (data: Record<string, string>) => {
          try {
            visitor.visitLine(data);
          } catch (error) {
            reject(error);
          }
        })
        .on('error', (error: Error) => {
          console.error(`💥 CSV parsing failed: ${error.message}`);
          reject(error);
        })
        .on('end', () => {
          const duration = Date.now() - startTime;
          console.log(`⏱️  Parsing completed in ${duration}ms`);
          
          try {
            const result = visitor.close();
            resolve(result);
          } catch (error) {
            reject(error);
          }
        });
    });
  }
}
