import { normalize } from 'node:path'
import type { Plugin } from 'vite'
import { createFilePath, writeFile, readDiretory } from './file-utils'
import { parseTree } from './get-tree-visitor'


let lock = false
const checkLock = () => lock
const setLock = (bool: boolean) => {
  lock = bool
}



function getConfig(init: {}, root: string): Config {
  return {
    fetchDirectory: "src/fetch",
    fetchTreeDirectory: "src", 
    fetchTreeGenFile: "fetchTree.gen.ts" 
  };
}

export interface Config {
  fetchDirectory: string; // src of file name patterns
  fetchTreeDirectory: string;
  fetchTreeGenFile: string;
}
export function fetchVite(options: Partial<Config> = {}): Plugin {

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
      const fetchDir = createFilePath([root], config.fetchDirectory);
      const fetchFiles = readDiretory(fetchDir.fullPath, { routeFileIgnorePattern: '__root'});

      for(const newFile of parseTree(fetchFiles)) {
        const path = createFilePath([root, config.fetchTreeDirectory], config.fetchTreeGenFile);
        writeFile({ fullPath: path.fullPath, content: newFile.content });
      }

      console.log('\u{2192} generated new fetch routes');
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
    const routesDirectoryPath = createFilePath([ROOT], userConfig.fetchDirectory).fullPath;

  
    if (filePath.startsWith(routesDirectoryPath) && !filePath.endsWith(userConfig.fetchTreeGenFile)) {
      await generate()
    }
  }

  return {
    name: 'fetch-vite',
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

