
export interface AvatarStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'AVATAR' }): Promise<T>;
}

export class AvatarStore {
  private _store: AvatarStoreConfig;

  constructor(store: AvatarStoreConfig) {
    this._store = store;
  }

  withStore(store: AvatarStoreConfig): AvatarStore {
    return new AvatarStore(store);
  }

  get store() { return this._store }


  async findAvatars(id: string[]): Promise<Avatar[]> {
    return await this._store.fetch<Avatar[]>(`avatars`, { 
      repoType: 'AVATAR',  
      method: 'POST',
      body: JSON.stringify({ id }),
    });
  }
}
export interface Avatar {
  id: string;
  version: string;
  externalId: string;
  avatarType: string;
  
  created: string;
  updated: string;
  colorCode: string;
  letterCode: string;
  displayName: string;
}

export type Avatars = Readonly<Record<string, Avatar>>;

export interface AvatarContextType {
  withAvatars(id: string[]): void;
  avatars: Avatars; 
}