
import { fileURLToPath, URL } from 'url'

function toPath(sourceCodePath: string) {
  return fileURLToPath(new URL(sourceCodePath, import.meta.url));
}

export const alias = [ 
  { find: '@dxs-ts/eveli-ide', replacement: toPath('./src')  },
  { find: '@/burger', replacement: toPath('./src/burger') },
]