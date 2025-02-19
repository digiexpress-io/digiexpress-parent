import csv  from 'csv-parser';
import { createReadStream } from 'node:fs'

class CsvReadVisitor {
  private _locales: Record<string, string[]> = {}; 
  private _index = 1;
  constructor() {
  }

  visitLine(object: Record<string, string>) {
    this._index++;
    const intlKey = this.visitIntlKey(object);
    if(!intlKey) {
      console.error('unknown column', this._index, object);
      return;
    }
    if(this.visitComment(intlKey)) {
      //console.warn('Skipping row because of comment', this._index, intlKey);
      return;
    }

    const locales: string[] = this.visitLocale(object);
    for(const locale of locales) {
      const intlValue = this.visitIntlValue(locale, object);
      this.visitIntlFileLine({locale, intlKey, intlValue});
    }
  }

  visitComment(keyOrValue: string): boolean {
    return keyOrValue.startsWith('//')
  }

  visitLocale(object: Record<string, string>): string[] {
    return Object.keys(object)
      .map(key => key.toLowerCase())
      .filter(key => key !== 'id')
      .filter(key => object[key] && !this.visitComment(object[key]))
      .filter(key => key.length == 2);
  }

  visitIntlKey(object: Record<string, string>): string | undefined {
    const idColumn = Object.entries(object).find(([key, value]) => key.toLowerCase() === 'id');
    if(idColumn) {
      return idColumn[1];
    }
    return undefined;
  }

  visitIntlValue(locale: string, object: Record<string, string>): string {
    return object[locale] ?? '';
  }
  
  visitIntlFileLine(props: {locale: string, intlKey: string, intlValue: string}) {
    if(!this._locales[props.locale]) {
      this._locales[props.locale] = [];
    }

    this._locales[props.locale].push(`'${props.intlKey}': '${props.intlValue}'`);
  }

  visitIntlFile(locale: string): IntlFile {
    const lines = this._locales[locale].map(line => `  ${line}`).join(',\r\n');
    const content = `export const ${locale} = {\r\n${lines}\r\n}`;
    return {
      fileName: `${locale}.ts`,
      content
    }
  }

  visitIndexFile(): IntlFile {
    const imports = Object.keys(this._locales)
      .map(locale => `import { ${locale} } from './${locale}'`)
      .join('\r\n');

    const content = `${imports}\r\nexport default { ${Object.keys(this._locales).join(', ')}}`;
    return { fileName: 'index.ts', content }
  }

  close(): IntlFile[] {
    const locales = Object.keys(this._locales).map((locale) => (this.visitIntlFile(locale)));
    return [this.visitIndexFile(), ...locales]
  }
}


export type IntlFile = {
  fileName: string;
  content: string;
}


export function parseCsv(path: string): Promise<IntlFile[]> {
  const visitor = new CsvReadVisitor();
  

  const promise = new Promise<IntlFile[]>((resolve, reject) => {
    createReadStream(path).pipe(csv())
    .on('data', (data) => visitor.visitLine(data))
    .on('error', (err)=> {
      reject(err);
    })
    .on('end', () => {
      resolve(visitor.close());
   })
 })




  return promise;
}