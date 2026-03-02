import { FsNode } from "@dxs-ts/fs-api";
import { Components } from "react-markdown";


export interface FsHelpProps {
  node: FsNode | undefined;
  remarkPlugins?: any[] | undefined;
  overrides?: Components;
}


export const helpMarkdownMock = `
# Tree Navigation: Getting started

Welcome to the Eveli Tree file explorer! This help section will guide you through using the tree navigation system effectively.

## Basic Navigation
- **Expand/Collapse**: Click the arrow icons next to folders to expand or collapse them
- **Select Node**: Click on any item to select it and view its details
- **Context Menu**: Right-click on items to access additional actions

## Tree Structure
The tree is organized hierarchically:
- **Root Level**: Main categories and top-level items
- **Folders**: Contain multiple sub-items
- **Files**: Individual content items

## Features
### Search
Use the search bar to quickly find specific items in the tree.

### Views
Switch between different view modes:
- **Properties**: View detailed information about selected items
- **History**: See change history and versions
- **References**: View connections and dependencies
- **Errors**: Check for validation issues

## Tips
> **Pro Tip**: Use keyboard shortcuts for faster navigation!

- Press \`Ctrl/Cmd + F\` to open search
- Use arrow keys to navigate between items
- Press \`Enter\` to expand/collapse selected folders

## Programmatic Access

You can also interact with the tree programmatically using the Eveli Tree API:

\`\`\`typescript
import { FsNode, useFs } from '@dxs-ts/fs-api';

// Example: Find and select a specific node
function useTreeNavigation() {
  const {
    activeNode,
    setActiveNode,
    openTabs,
    setActiveTab
  } = useFs();

  const selectNodeById = (nodeId: string) => {
    // Navigate to a specific node
    const targetNode = findNodeById(nodeId);
    if (targetNode) {
      setActiveNode(targetNode);
      setActiveTab(targetNode.path);
    }
  };

  const findNodeById = (id: string): FsNode | null => {
    // Implementation to search tree structure
    return activeNode?.children?.find(child => child.id === id) || null;
  };

  return { selectNodeById, currentNode: activeNode };
}
\`\`\`

## Need More Help?
If you encounter any issues or need additional assistance, please contact support.
[Helpdesk](http://www.google.com)
[Official docs](https://www.thegooddocsproject.dev/)
`;
