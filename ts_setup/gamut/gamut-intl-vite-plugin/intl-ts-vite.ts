import { normalize } from 'node:path'
import type { Plugin } from 'vite'
import { createFilePath, writeFile } from './file-utils'
import { IntlFile, parseCsv } from './parse-csv'


let lock = false
const checkLock = () => lock
const setLock = (bool: boolean) => {
  lock = bool
}


  /*
  return [{
    name: 'replace-my-text', // Name for the plugin
    enforce: 'pre',          // Run this plugin before other transformations
    transform(code, id) {
      // Replace MY_TEXT_EXAMPLE with "Hello world!" in JavaScript and TypeScript files
      if (/\.(js|ts|jsx|tsx)$/.test(id)) {
        console.log(id);
      }
      return null; // Return null if no transformation is applied
    },
  }];*/


function getConfig(init: {}, root: string): Config {
  return {
    intlDirectory: "src/intl",
    csv: "intl.csv" 
  };
}

export interface Config {
  intlDirectory: string; // target
  csv: string;
}
export function intlTsVite(options: Partial<Config> = {}): Plugin {

  let ROOT: string = process.cwd()
  let userConfig = options as Config

  const generate = async () => {
    if (checkLock()) {
      return
    }
    setLock(true)
    try {
      const config = userConfig;
      const root = process.cwd();
      const csvFile = createFilePath([root], config.csv);
      const intlFiles: IntlFile[] = await parseCsv(csvFile.fullPath);
    
      for(const newFile of intlFiles) {
        const path = createFilePath([root, config.intlDirectory], newFile.fileName);
        writeFile({ fullPath: path.fullPath, content: newFile.content });
      }
      console.log('\u{1F30D} generated new intl');
    } catch (err) {
      console.error(err)
      console.info()
    } finally {
      setLock(false)
    }
  }

  const handleFile = async (
    file: string,
    event: 'create' | 'update' | 'delete',
  ) => {
    const filePath = normalize(file)    
    const routesDirectoryPath = createFilePath([ROOT], userConfig.csv).fullPath;
    if (filePath.startsWith(routesDirectoryPath)) {
      await generate()
    }
  }

  return {
    name: 'intl-ts-vite',
    async watchChange(id, { event }) {
      await handleFile(id, event)
    },
    async configResolved(config) {
      userConfig = getConfig(options, ROOT)
      ROOT = config.root
      await generate()
    },
  }
}

