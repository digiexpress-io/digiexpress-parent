import React from 'react';
import { HdesFileSystem, HdesFileSystemCommand, HdesFileSystemStore, HdesFile, HdesFileId } from './FileSystemTypes';
import { useBackend } from 'descriptor-backend';


export type AbsolutePath = string;

export interface HierarchicalFileSystem {
  id: string;
  version: string,
  type: string;
  fs: HdesFileSystem;

  nodes: HierarchicalFileSystemNode[];
}


export interface HierarchicalFileSystemNode {
  depth: number;
  absolutePath: string;
  parentPath: string | undefined;
  file: HdesFile;
  nodeName: string;
  children: HierarchicalFileSystemNode[];
}

interface ReducedNodes {
  byPath: Record<AbsolutePath, HierarchicalFileSystemNode>;
  byFolder: Record<AbsolutePath, HierarchicalFileSystemNode[]>;
}

function getFileMeta(file: HdesFile) {
  const fragments = file.absolutePath.split("/");
  const depth = fragments.length;
  const parentPath = fragments.length > 1 ? fragments.filter((t, index) => index < depth -1).join("/"): undefined;

  if(file.fileType === 'FOLDER') {
    // no file name
    const folder = file.absolutePath;
    return {
      parentPath,
      fileName: fragments[fragments.length - 1], 
      absolutePath: file.absolutePath, 
      depth, 
      folder };
  }

  const folder = file.absolutePath;
  const fileName = file.fileName + "." + file.fileType;
  const absolutePath = file.absolutePath + "/" + fileName;
  return {fileName, absolutePath, depth: depth + 1, folder, parentPath};
}

function init(fs: HdesFileSystem): HierarchicalFileSystem {
  const all_nodes: ReducedNodes = fs.tree.files
    .reduce<ReducedNodes>((result, file) => {

      const { absolutePath, depth, folder, parentPath, fileName } = getFileMeta(file);      
      const node: HierarchicalFileSystemNode = { absolutePath, file, children: [], depth, parentPath, nodeName: fileName };
      result.byPath[absolutePath] = node;


      if(file.fileType === 'FOLDER') {
        if(!parentPath) {
          return result;
        }
        if (!result.byFolder[parentPath]) {
          result.byFolder[parentPath] = [];
        }
        result.byFolder[parentPath].push(node);
        return result;
      }

      if (!result.byFolder[folder]) {
        result.byFolder[folder] = [];
      }
      result.byFolder[folder].push(node);
    
      return result;
    }, { byPath: {}, byFolder: {} });

  const nodes: HierarchicalFileSystemNode[] = Object.keys(all_nodes.byFolder).filter(folder => {
    const node = all_nodes.byPath[folder];
    const children = all_nodes.byFolder[folder];
    
    node.children.push(...children);
    return node.depth === 1;
  }).map(folder => all_nodes.byPath[folder]);

  return Object.freeze({ id: fs.id, version: fs.version, type: fs.type, fs, nodes });
}

export interface FileSystemContextType {
  fs: HierarchicalFileSystem;
  handleUpdate: (update: HdesFileSystemCommand<any>) => Promise<void>;
}

export const FileSystemContext = React.createContext<FileSystemContextType>({} as any);

export const FileSystemProvider: React.FC<{ children: React.ReactNode; }> = ({ children }) => {
  const backend = useBackend();
  const store = React.useMemo(() => new HdesFileSystemStore(backend.store), [backend]);
  const [fs, setFs] = React.useState<HierarchicalFileSystem>();

  React.useEffect(() => {
    store.get().then(data => setFs(init(data)))
  }, []);

  const handleUpdate = React.useCallback(async (update: HdesFileSystemCommand<any>) => {
    const next = await store.update(update.id, [update]);
    setFs(init(next));
  }, [setFs]);

  const contextValue: FileSystemContextType = React.useMemo(() => {
    return Object.freeze({ fs: fs ?? {} as any, handleUpdate });
  }, [fs, handleUpdate]);

  return (<FileSystemContext.Provider value={contextValue}>
    {fs && children}
  </FileSystemContext.Provider>);
}


export function useFileSystem() {
  const result: FileSystemContextType = React.useContext(FileSystemContext);
  return result;
}