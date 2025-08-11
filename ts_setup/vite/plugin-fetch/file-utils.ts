import { writeFileSync, existsSync, mkdirSync, readdirSync, Dirent } from 'node:fs'
import { resolve, dirname, posix } from 'node:path'



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

export type SourceFile = {
  fileName: string,
  relativePath: string,
  dirent: Dirent
}

export function readDiretory(
  fullPath: string, 
  config: {
    routeFilePrefix?: string, 
    routeFileIgnorePrefix?: string, 
    routeFileIgnorePattern?: string
  }
): SourceFile[] {
  const { routeFilePrefix, routeFileIgnorePrefix, routeFileIgnorePattern } = config;
  const routeFileIgnoreRegExp = new RegExp(routeFileIgnorePattern ?? '', 'g')
  return readdirSync(fullPath, { withFileTypes: true, recursive: false })
    .filter((d) => {
      console.log("Parsing files", d.name)
      if (
        d.name.startsWith('.') ||
        (routeFileIgnorePrefix && d.name.startsWith(routeFileIgnorePrefix))
      ) {
        return false
      }
      if (routeFilePrefix) {
        return d.name.startsWith(routeFilePrefix)
      }
      if (routeFileIgnorePattern) {
        return !d.name.match(routeFileIgnoreRegExp)
      }
      return true
    })
    .filter(dirent => {
      const fileName = posix.join(fullPath, dirent.name)
      return fileName.match(/\.(tsx|ts|jsx|js)$/)
    })
    .map((dirent) => {
      const fileName = posix.join(fullPath, dirent.name)
      const relativePath = posix.join('./', dirent.name)
      const result: SourceFile = {
        fileName,
        relativePath,
        dirent
      }
      return result;
    });
}
