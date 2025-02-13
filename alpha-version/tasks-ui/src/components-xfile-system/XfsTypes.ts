import { HdesFile, HdesFileSystem } from './HdesFileSystemTypes';

export type AbsolutePath = string;

export interface Xfs {
  id: string;
  version: string,
  type: string;
  fs: HdesFileSystem;

  nodes: XfsNode[];
}


export interface XfsNode {
  file: HdesFile;
  
  depth: number;
  absolutePath: string;
  parentPath: string | undefined;

  nodeName: string;
  children: XfsNode[];
}

interface ReducedNodes {
  byPath: Record<AbsolutePath, XfsNode>;
  byFolder: Record<AbsolutePath, XfsNode[]>;
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

export function getXfsInstance(fs: HdesFileSystem): Xfs {
  const all_nodes: ReducedNodes = fs.tree.files
    .reduce<ReducedNodes>((result, file) => {

      const { absolutePath, depth, folder, parentPath, fileName } = getFileMeta(file);      
      const node: XfsNode = { absolutePath, file, children: [], depth, parentPath, nodeName: fileName };
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

  const nodes: XfsNode[] = Object.keys(all_nodes.byFolder).filter(folder => {
    const node = all_nodes.byPath[folder];
    const children = all_nodes.byFolder[folder];
    
    node.children.push(...children);
    return node.depth === 1;
  }).map(folder => all_nodes.byPath[folder]);

  return Object.freeze({ id: fs.id, version: fs.version, type: fs.type, fs, nodes });
}
