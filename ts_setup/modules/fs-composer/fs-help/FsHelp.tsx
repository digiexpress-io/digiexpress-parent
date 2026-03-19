import React from 'react';
import { Typography, Box } from '@mui/material';
import { OpenInNew as OpenInNewIcon } from '@mui/icons-material';
import ReactMarkdown from 'react-markdown';

import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsHelpProps } from './FsHelpProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsHelpRoot } from './useUtilityClasses';


export const FsHelp: React.FC<FsHelpProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  const linkRenderer = (props: any) => {
    return (
      <a href={props.href} target="_blank" rel="noreferrer" className={classes.externalLink}>
        {props.children}
        <OpenInNewIcon fontSize="small" />
      </a>
    );
  };

  return (
    <FsPanel title="Help" icon={<FsIcon icon={FsIcons.Help} large />} activeDirent={true}>
      <FsHelpRoot className={classes.root} ownerState={ownerState}>
        <ReactMarkdown components={
          {
            h1: (props) => (<Typography className={classes.h1}>{props.children}</Typography>),
            h2: (props) => (<Typography className={classes.h2}>{props.children}</Typography>),
            h3: (props) => (<Typography className={classes.h3}>{props.children}</Typography>),
            h4: (props) => (<Typography className={classes.h4}>{props.children}</Typography>),
            h5: (props) => (<Typography className={classes.h5}>{props.children}</Typography>),
            h6: (props) => (<Typography className={classes.h6}>{props.children}</Typography>),
            p: (props) => (<Typography className={classes.paragraph}>{props.children}</Typography>),
            li: (props) => (<li className={classes.listItem}><Typography component="span" variant={'body1'}>{props.children}</Typography></li>),
            a: linkRenderer,
            code: (props) => (
              <Box component="code" className={classes.codeBlock}>
                <Box className={classes.codeContent}>
                  {props.children}
                </Box>
              </Box>
            ),
            ...(props.overrides ?? {})
          }}
        >
          {helpMarkdownMock}
        </ReactMarkdown>
      </FsHelpRoot>
    </FsPanel>
  );
};


const helpMarkdownMock = `
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
import { FsDirent, useFs } from '@dxs-ts/fs-api';

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

  const findNodeById = (id: string): FsDirent | null => {
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
