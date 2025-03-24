
import { fileURLToPath, URL } from 'url'

function toPath(sourceCodePath: string) {
  return fileURLToPath(new URL(sourceCodePath, import.meta.url));
}

export const alias = [ 
  { find: '@dxs-ts/eveli-ide', replacement: toPath('./src')  },
  { find: '@dxs-ts/eveli-fetch', replacement: toPath('./src/eveli-fetch')  },
  { find: '@', replacement: toPath('./src') },
]