import { SourceFile } from "./file-utils";


function cleanPath(path: string) {
  return path.replace(/\/{2,}/g, '/')
}
function determineInitialRoutePath(routePath: string) {
  return cleanPath(`${routePath.split('.').join('/')}`) || ''
}
function capitalize(s: string) {
  if (typeof s !== 'string') return ''
  return s.charAt(0).toUpperCase() + s.slice(1)
}
function removeUnderscores(s?: string) {
  return s?.replaceAll(/(^_|_$)/gi, '').replaceAll(/(\/_|_\/)/gi, '/')
}
function routePathToVariable(routePath: string): string {
  return (
    removeUnderscores(routePath)
      ?.replace(/\/\$\//g, '/splat/')
      .replace(/\$$/g, 'splat')
      .replace(/\$/g, '')
      .split(/[/-]/g)
      .map((d, i) => (i > 0 ? capitalize(d) : d))
      .join('')
      .replace(/([^a-zA-Z0-9]|[.])/gm, '')
      .replace(/^(\d)/g, 'R$1') ?? ''
  )
}
function removeTwoExt(d: string, webMethod: string) {
  const first = removeOneExt(d);
  const upperCased = first.toUpperCase();
  if(upperCased.endsWith(`.${webMethod.toUpperCase()}`)) {
    return removeOneExt(first);
  }
  return first;
}
function removeOneExt(d: string) {
  return d.substring(0, d.lastIndexOf('.')) || d
}

function urlParams(routePath: string): string[] {
  const possibleKeys = routePath.split('.')
    .filter(key => key.startsWith("$"))
    .map(key => key.substring(1));
  return possibleKeys;
}

interface Ast {
  id: string;
  routePath: string
  webMethod: string,
  variableName: string;
  updateName: string;
  relativePath: string;
  pathParams: string[];
}


class TreeVisitor {
  private _hookEntries: string[] = []; 
  private _hookUpdateEntries: string[] = []; 
  private _hookTypeEntries: string[] = []; 
  private _importEntries: string[] = []; 
  constructor() {
  }

  visit(src: SourceFile) {
    const webMethod = this.visitWebMethod(src);
    const filePathNoExt = removeTwoExt(src.relativePath, webMethod)
    const routePath = determineInitialRoutePath(filePathNoExt);
    const variableName = routePathToVariable(routePath) + webMethod;
    const id = `${routePath}.${webMethod}`;
    const updateName = `${variableName}Route`;
    const pathParams = urlParams(filePathNoExt);
    const ast: Ast = {
      id,
      routePath,
      webMethod,
      updateName,
      variableName,
      relativePath: src.relativePath,
      pathParams
    }
    this.visitAst(ast);
  }

  visitAst(ast: Ast) {
    const { variableName, id, routePath, webMethod, relativePath, updateName } = ast;

    const importEntry = `import { Hook as ${variableName} } from './fetch/${relativePath}'`;
    const hookTypeEntry = `
    '${id}': {
      id: '${id}',
      path: '${routePath}',
      method: '${webMethod}',
      params: {${ast.pathParams.map((key) => `${key}: string`).join(', ')}},
      hook: typeof ${updateName}
    }`;

    const hookEntry = `
const ${updateName} = ${variableName}.update({
  path: '${routePath}',
  method: '${webMethod}',
})`;

    this._hookEntries.push(hookEntry);
    this._hookUpdateEntries.push(`'${id}': ${updateName}`);
    this._importEntries.push(importEntry);
    this._hookTypeEntries.push(hookTypeEntry);
  }

  visitWebMethod(src: SourceFile): 'GET' | 'POST' | 'DELETE' | 'PUT' {
    const filePathNoExt = removeOneExt(src.relativePath);
    const webMethod = filePathNoExt.substring(filePathNoExt.lastIndexOf('.') + 1).toUpperCase();
    switch(webMethod) {
      case 'GET': return 'GET';
      case 'POST': return 'POST';
      case 'DELETE': return 'DELETE';
      case 'PUT': return 'PUT'; 
      default: return 'GET';
    }
  }

  visitUpdateTree() {
    return `export const tree = RootHook.update({${this._hookUpdateEntries.join(', ')}})`
  }

  visitHookByPath() {
    return `
declare module '@dxs-ts/eveli-fetch' {
  interface HookByPath {
${this._hookTypeEntries.join('\r\n')}
  }
}`
  }

  close(): TreeFile[] {
    const result = [
      "import { Hook as RootHook } from './fetch/__root'",
      ...this._importEntries,'','',  
      ...this._hookEntries,'','',
      this.visitHookByPath(), 
      this.visitUpdateTree()
    ].join('\r\n');

    return [{
      fileName: 'fetchTree.gen.ts',
      content: result
    }];
  }
}

export type TreeFile = {
  fileName: string;
  content: string;
}

export function parseTree(files: SourceFile[]): TreeFile[] {
  const visitor = new TreeVisitor();
  files.forEach(src => {
    visitor.visit(src);
  })
  return visitor.close();
}