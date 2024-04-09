

export interface Avatar {
  origin: string;
  twoLetterCode: string;
  displayName: string | undefined;
  color: string;
  index: number; // duplication index, same initial but the rest of letters are different
}

export interface Avatars {
  values: Record<string, Avatar>;
}

export interface AvatarReducer {
  withAvatar: (letters: string) => void;
  withAvatars: (letters: string[]) => void;
}

export interface AvatarContextType {
  reducer: AvatarReducer;
  avatars: Avatars; 
}