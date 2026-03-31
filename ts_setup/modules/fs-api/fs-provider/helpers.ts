import { FsDirent } from "../fs-types";
import { mockFsData } from "../mock-fs-data";




function collectDirents(result: Record<string, FsDirent.Dirent>, node: FsDirent.Dirent): void {
  result[node.id] = node;
  node.children.forEach(child => collectDirents(result, child));
}

function flattenDirents(nodes: FsDirent.Dirent[]): Record<string, FsDirent.Dirent> {
  const result: Record<string, FsDirent.Dirent> = {};
  nodes.forEach(node => collectDirents(result, node));
  return result;
}

export const ALL_DIRENTS = flattenDirents(mockFsData);

export function collectArticles(nodes: FsDirent.Dirent[]): FsDirent.SelectOption[] {
  const result: FsDirent.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'article') { result.push({ value: node.id, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectArticles(node.children)); }
  });
  return result;
}

export function collectFlows(nodes: FsDirent.Dirent[]): FsDirent.SelectOption[] {
  const result: FsDirent.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'flow') { result.push({ value: node.name, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectFlows(node.children)); }
  });
  return result;
}

export function collectDialobs(nodes: FsDirent.Dirent[]): FsDirent.SelectOption[] {
  const result: FsDirent.SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'dialob') { result.push({ value: node.id, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectDialobs(node.children)); }
  });
  return result;
}

export function collectLanguages(nodes: FsDirent.Dirent[]): string[] {
  const result: string[] = [];
  nodes.forEach(node => {
    if (node.type === 'language') { result.push(node.name.replace('.language', '')); }
    if (node.children && node.children.length > 0) { result.push(...collectLanguages(node.children)); }
  });
  return result;
}

const ALL_CONFIG_OPTIONS: FsDirent.SelectOption[] = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

export function getConfigOptionsForType(type: FsDirent.Type): FsDirent.SelectOption[] {
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