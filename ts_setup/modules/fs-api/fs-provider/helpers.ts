import { Fs } from "../fs-types";
import { mockFsData } from "../mock-fs-data";




function collectDirents(result: Record<string, Fs.DirentBase>, node: Fs.DirentBase): void {
  result[node.id] = node;
  node.children.forEach(child => collectDirents(result, child));
}

function flattenDirents(nodes: Fs.DirentBase[]): Record<string, Fs.DirentBase> {
  const result: Record<string, Fs.DirentBase> = {};
  nodes.forEach(node => collectDirents(result, node));
  return result;
}

export const ALL_DIRENTS = flattenDirents(mockFsData);

export function collectArticles(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'article') { result.push({ value: node.id, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectArticles(node.children)); }
  });
  return result;
}

export function collectFlows(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'flow') { result.push({ value: node.name, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectFlows(node.children)); }
  });
  return result;
}

export function collectDialobs(nodes: Fs.DirentBase[]): Fs.SelectOption[] {
  const result: Fs.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'dialob') { result.push({ value: node.id, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectDialobs(node.children)); }
  });
  return result;
}

export function collectLanguages(nodes: Fs.DirentBase[]): string[] {
  const result: string[] = [];
  nodes.forEach(node => {
    if (node.type === 'language') { result.push(node.name.replace('.language', '')); }
    if (node.children && node.children.length > 0) { result.push(...collectLanguages(node.children)); }
  });
  return result;
}

export function collectLabels(propsMap: Record<string, Fs.Props>): string[] {
  const labelSet = new Set<string>();
  Object.values(propsMap).forEach(entry => {
    entry.labels.forEach(l => labelSet.add(l.value));
  });
  return Array.from(labelSet).sort();
}

const ALL_CONFIG_OPTIONS: Fs.SelectOption[] = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

export function getConfigOptionsForType(type: Fs.Type): Fs.SelectOption[] {
  switch (type) {
    case 'link':
    case 'phone': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode' || o.value === 'disabledMode');
    }
    case 'service':
    case 'article': {
      return ALL_CONFIG_OPTIONS;
    }
    case 'language': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode');
    }
    case 'page': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode' || o.value === 'devMode');
    }
    case 'printout': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode');
    }
    default: {
      return [];
    }
  }
}