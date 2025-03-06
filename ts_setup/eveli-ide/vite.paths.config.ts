
import { fileURLToPath, URL } from 'url'

function toPath(sourceCodePath: string) {
  return fileURLToPath(new URL(sourceCodePath, import.meta.url));
}

export const alias = [ 
  { find: '@dxs-ts/eveli-ide', replacement: toPath('./src')  },
  { find: '@dxs-ts/eveli-fetch', replacement: toPath('./src/eveli-fetch')  },
  { find: '@/burger', replacement: toPath('./src/burger') },
  { find: '@/feedback', replacement: toPath('./src/feedback') },
  //{ find: '@dxs-ts/gamut', replacement: toPath('../gamut/src') },
]