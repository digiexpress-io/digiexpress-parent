import { TreeNode } from "../../eveli-tree-api";




export function filterTreeNodes(nodes: TreeNode[], searchTerm: string): TreeNode[] {
  if (!searchTerm.trim() || searchTerm.trim().length < 3) {
    return nodes;
  }

  const filtered: TreeNode[] = [];

  for (const node of nodes) {
    const nameMatches = node.name.toLowerCase().includes(searchTerm.toLowerCase());
    const childMatches = node.children ? filterTreeNodes(node.children, searchTerm) : [];

    if (nameMatches || childMatches.length > 0) {
      filtered.push({
        ...node,
        expanded: childMatches.length > 0 ? true : node.expanded,
        children: childMatches.length > 0 ? childMatches : node.children
      });
    }
  }

  return filtered;
}