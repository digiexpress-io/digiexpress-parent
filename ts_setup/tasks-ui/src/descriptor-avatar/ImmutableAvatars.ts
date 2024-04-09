import { Avatars, Avatar } from './avatar-types';
import { PALETTE_COLORS } from 'components-colors';


export interface Init {
  values: Record<string, Avatar>;
  cache?: Record<TwoLetterCode, Origin[]>;
  cache_colors?: string[]; 
}

export type Origin = string;
export type TwoLetterCode = string;

const TOTAL_COLORS = 500;
function initColor(index: number) {
  const color = index * (360 / TOTAL_COLORS) % 360;
  return "hsl( " + color + ", 100%, 50% )";
}


export class ImmutableAvatars implements Avatars {
  private _values: Record<Origin, Avatar>;
  private _cache: Record<TwoLetterCode, Origin[]>;
  private _cache_colors: readonly string[];

  constructor(init: Init) {
    this._values = Object.freeze(init.values);
    this._cache = Object.freeze(init.cache ?? {});
    this._cache_colors = Object.freeze(init.cache_colors ?? []);
    
  }

  get values() { return this._values }

  withAvatar(origin: Origin): ImmutableAvatars {
    return this.withAvatars([origin]);
  }

  getNextColor(cache_colors: string[]): string {
    // try first polite colors
    for(let index = cache_colors.length; index < PALETTE_COLORS.length; index++) {
      const newColor = PALETTE_COLORS[index];
      if(!cache_colors.includes(newColor)) {
        return newColor;
      }
    }

    // fallback
    const index = cache_colors.length + 1;
    const colorIndex = index * 50;

    const newColor = initColor(colorIndex);
    if(!cache_colors.includes(newColor)) {
      return newColor;
    }

    for(let add = 0; add < 1000; add++) {
      const addColor = initColor(colorIndex+add);
      if(!cache_colors.includes(addColor)) {
        return addColor;
      }
    }
    return newColor;
  }

  withAvatars(all: string[]): ImmutableAvatars {
    const newData: Avatar[] = [];
    const cache_colors = [...this._cache_colors];

    for(const origin of all) {
      if(this._values[origin]) {
        continue;
      }

      const twoLetterCode: TwoLetterCode = resolveAvatar(origin);
      const codeIndex = this._cache[twoLetterCode] ? this._cache[twoLetterCode].length + 1 : 0;
      const color = this.getNextColor(cache_colors);
      

      const avatar: Avatar = Object.freeze({ origin, twoLetterCode, color, index: codeIndex, displayName: undefined });
      cache_colors.push(color);
      newData.push(avatar);
    }
  
    if(all.length === 0) {
      return this;
    }

    const values: Record<Origin, Avatar> = { ...this._values };
    const cache: Record<TwoLetterCode, Origin[]> = { ...this._cache };

    for(const avatar of newData) {
      values[avatar.origin] = avatar;

      if(!cache[avatar.twoLetterCode]) {
        cache[avatar.twoLetterCode] = [];
      }
      cache[avatar.twoLetterCode].push(avatar.origin);
    }

    return new ImmutableAvatars({ values, cache, cache_colors });
  }
}

export function resolveAvatar(role: string): TwoLetterCode {  
  const words: string[] = role
    .replaceAll("-", " ")
    .replaceAll("_", " ")
    .replace(/([A-Z])/g, ' $1')
    .replaceAll("  ", " ")
    .trim().split(" ");
  const result: string[] = [];

  for (const word of words) {
    if (result.length >= 2) {
      break;
    }

    if (word && word.length) {
      const firstLetter = word.substring(0, 1);
      result.push(firstLetter.toUpperCase());
    }
  }
  
  return result.join("");
}