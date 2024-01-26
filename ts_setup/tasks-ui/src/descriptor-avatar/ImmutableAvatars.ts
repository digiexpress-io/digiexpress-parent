
import { withColors } from 'components-colors';
import { Avatars, Avatar } from './avatar-types';

export interface Init {
  values: Record<string, Avatar>;
  cache?: Record<TwoLetterCode, Origin[]>;
  index?: number; 
}

export type Origin = string;
export type TwoLetterCode = string;


export class ImmutableAvatars implements Avatars {
  private _values: Record<Origin, Avatar>;
  private _cache: Record<TwoLetterCode, Origin[]>;
  private _index: number;

  constructor(init: Init) {
    this._values = Object.freeze(init.values);
    this._cache = Object.freeze(init.cache ?? {});
    this._index = init.index ?? 0;
  }

  get values() { return this._values }

  withAvatar(origin: Origin): ImmutableAvatars {
    return this.withAvatars([origin]);
  }

  withAvatars(all: string[]): ImmutableAvatars {
    const newData: Avatar[] = [];

    let runningIndex = 0;
    for(const origin of all) {

      if(this._values[origin]) {
        continue;
      }

      const twoLetterCode: TwoLetterCode = resolveAvatar(origin);
      const originIndex = this._index + runningIndex;
      const codeIndex = this._cache[twoLetterCode] ? this._cache[twoLetterCode].length + 1 : 0;
      const colorIndex = originIndex + codeIndex;
      const color = withColors([twoLetterCode], colorIndex)[0].color;
      const avatar: Avatar = Object.freeze({ origin, twoLetterCode, color, index: codeIndex });
      newData.push(avatar);
      runningIndex++;
    }
  
    if(runningIndex === 0) {
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

    return new ImmutableAvatars({ values, cache });
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