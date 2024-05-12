
/**
 * Data types
 */
export type HdesFileType = (
  'DIALOB' |
  'DT' | 'FLOW_TASK' | 'FLOW' |
  'TOPIC_BINDING' | 'TOPIC' | 'TOPIC_ACTION' |
  'INTL' | 'FOLDER'
)
export type HdesFileSystemId = string;
export type HdesFileId = string;

export interface HdesFileSystem {
  id: HdesFileSystemId;
  version: string;
  type: string
  tree: HdesFileTree;
}

export interface HdesFileTree {
  files: HdesFile[];
}

export interface HdesFile {
  id: HdesFileId;
  absolutePath: string; // path to file
  mountTo: string | undefined; // technical asset GID
  fileName: string | undefined;
  fileType: HdesFileType;
}

/**
 * Command types
 */
export type TaskCommandType = 'CREATE_FOLDER' | 'CREATE_FILE' | 'MOVE_FILE' | 'RENAME_FILE_NAME' | 'DELETE_FILE';

export interface HdesFileSystemCommand<T extends TaskCommandType> {
  id: HdesFileSystemId;
  commandType: T;
}
export interface HdesCreateFolder extends HdesFileSystemCommand<'CREATE_FOLDER'> {
  absolutePath: string; // path to file
}
export interface HdesCreateFile extends HdesFileSystemCommand<'CREATE_FILE'> {
  mountTo: string; // technical asset GID
  fileName: string | undefined;
  fileType: HdesFileType;
}



/**
 *  Store types
 */
export interface HdesFileSystemStoreConfig {
  fetch<T>(path: string, init: RequestInit & { notFound?: () => T, repoType: 'HDES' }): Promise<T>;
}
export interface HdesFileSystemStoreType {
  get(): Promise<HdesFileSystem>
  update(id: HdesFileSystemId, commands: HdesFileSystemCommand<any>[]): Promise<HdesFileSystem>
}


/**
 * Store backend
 */
export class HdesFileSystemStore implements HdesFileSystemStoreType {
  private _store: HdesFileSystemStoreConfig;

  constructor(store: HdesFileSystemStoreConfig) {
    this._store = store;
  }

  withStore(store: HdesFileSystemStoreConfig): HdesFileSystemStore {
    return new HdesFileSystemStore(store);
  }

  async update(id: HdesFileSystemId, commands: HdesFileSystemCommand<any>[]): Promise<HdesFileSystem> {
    return await this._store.fetch<HdesFileSystem>(`file-system/${id}`, {
      method: 'PUT',
      body: JSON.stringify(commands),
      repoType: 'HDES'
    });
  }

  async get(): Promise<HdesFileSystem> {
    //const fs = await this._store.fetch<HdesFileSystem>(`file-system`, { repoType: 'HDES' });
    return test_data;
  }
}

let index = 0;
function uuid(): string {
  index++;
  return index + "";
}

const ROOT = 'cities/ankh-morpork';



const test_data: HdesFileSystem = {
  id: uuid(),
  version: uuid(),
  type: "FILE_SYSTEM",
  tree: {
    files: [
      { absolutePath: "cities", fileType: "FOLDER", id: uuid(), mountTo: uuid(), fileName: "" },
      { absolutePath: ROOT, fileType: "FOLDER", id: uuid(), mountTo: uuid(), fileName: "" },
      { absolutePath: ROOT + "/topics", fileName: "", fileType: "FOLDER", id: uuid(), mountTo: uuid(), },
      { absolutePath: ROOT + "/topics/index", fileName: "", fileType: "FOLDER", id: uuid(), mountTo: uuid(), },
      { absolutePath: ROOT + "/topics/general-message", fileName: "", fileType: "FOLDER", id: uuid(), mountTo: uuid(), },
      { absolutePath: ROOT + "/localizations/forms-translations", fileName: "", fileType: "FOLDER", id: uuid(), mountTo: uuid(), },
      { absolutePath: ROOT + "/localizations/wrench-translations", fileName: "", fileType: "FOLDER", id: uuid(), mountTo: uuid(), },
      { absolutePath: ROOT + "/localizations/topic-translations", fileName: "", fileType: "FOLDER", id: uuid(), mountTo: uuid(), },



      {
        fileType: "TOPIC_BINDING", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/topics",
        fileName: "topic-binding-for-en-locale"
      },

      // topics
      {
        fileType: "TOPIC", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/topics/index",
        fileName: "index-topic"
      },
      {
        fileType: "TOPIC_ACTION", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/topics/index/general-message",
        fileName: "general-message-action"
      },
      {
        fileType: "FLOW", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/topics/index/general-message",
        fileName: "general-message-form"
      },
      {
        fileType: "FLOW", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/topics/index/general-message",
        fileName: "general-message-flow"
      },
      {
        fileType: "DT", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/topics/index/general-message",
        fileName: "general-message-dt"
      },
      {
        fileType: "FLOW_TASK", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/topics/index/general-message",
        fileName: "general-message-create-task"
      },



      // form locale folder
      {
        fileType: "INTL", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/localizations/forms-translations",
        fileName: "general-message-form-intl"
      },
      {
        fileType: "INTL", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/localizations/forms-translations",
        fileName: "education-grant-form-intl"
      },

      // topic locale folder
      {
        fileType: "INTL", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/localizations/topic-translations",
        fileName: "index"
      },
      {
        fileType: "INTL", id: uuid(), mountTo: uuid(),
        absolutePath: ROOT + "/localizations/topic-translations",
        fileName: "education"
      },
    ]
  }
}
