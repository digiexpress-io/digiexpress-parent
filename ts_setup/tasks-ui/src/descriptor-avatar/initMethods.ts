import React from 'react';

import { Avatar, AvatarReducer } from './avatar-types';
import { ImmutableAvatars } from './ImmutableAvatars';


export function initAvatars(): ImmutableAvatars {
  return new ImmutableAvatars({ values: {} });
}

export function initReducer(
  avatars: ImmutableAvatars,
  setAvatars: React.Dispatch<React.SetStateAction<ImmutableAvatars>>,
): AvatarReducer {
  
  const result: AvatarReducer = {
    withAvatar: (letters) => {
      const state: ImmutableAvatars = avatars.withAvatar(letters);
      setAvatars(state);
      return state.values[letters];
    },
    withAvatars(all) {
      const state: ImmutableAvatars = avatars.withAvatars(all);
      setAvatars(state);
      return all.map(letters => state!.values[letters]);
    },
  };
  return Object.freeze(result);
}
