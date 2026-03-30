import { FsDirent, FsDirentType, SelectOption } from "../fs-types";
import { mockFsData } from "../mock-fs-data";




function collectDirents(result: Record<string, FsDirent>, node: FsDirent): void {
  result[node.id] = node;
  node.children.forEach(child => collectDirents(result, child));
}

function flattenDirents(nodes: FsDirent[]): Record<string, FsDirent> {
  const result: Record<string, FsDirent> = {};
  nodes.forEach(node => collectDirents(result, node));
  return result;
}

export const ALL_DIRENTS = flattenDirents(mockFsData);

export function collectArticles(nodes: FsDirent[]): SelectOption[] {
  const result: SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'article') { result.push({ value: node.id, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectArticles(node.children)); }
  });
  return result;
}

export function collectFlows(nodes: FsDirent[]): SelectOption[] {
  const result: SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'flow') { result.push({ value: node.name, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectFlows(node.children)); }
  });
  return result;
}

export function collectDialobs(nodes: FsDirent[]): SelectOption[] {
  const result: SelectOption[] = [];
  nodes.forEach(node => {
    if (node.type === 'dialob') { result.push({ value: node.id, label: node.name }); }
    if (node.children && node.children.length > 0) { result.push(...collectDialobs(node.children)); }
  });
  return result;
}

export function collectLanguages(nodes: FsDirent[]): string[] {
  const result: string[] = [];
  nodes.forEach(node => {
    if (node.type === 'language') { result.push(node.name.replace('.language', '')); }
    if (node.children && node.children.length > 0) { result.push(...collectLanguages(node.children)); }
  });
  return result;
}

const ALL_CONFIG_OPTIONS: SelectOption[] = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

export function getConfigOptionsForType(type: FsDirentType): SelectOption[] {
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