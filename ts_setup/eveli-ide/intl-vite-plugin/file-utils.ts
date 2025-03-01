import { writeFileSync, existsSync, mkdirSync } from 'node:fs'
import { resolve, dirname } from 'node:path'



export function writeFile(props: {
  fullPath: string;
  content: string;
}): void {
  const directory = dirname(props.fullPath)
  if (!existsSync(directory)) {
    mkdirSync(directory)
  }
  writeFileSync(props.fullPath, props.content, { flag: 'w' });
}


export type FilePath = {
  fullPath: string;
  fileName: string;
}

export function createFilePath(directory: string[], fileName: string): FilePath {
  const fullPath = resolve(...directory, fileName)
  return {
    fullPath,
    fileName
  }
}
