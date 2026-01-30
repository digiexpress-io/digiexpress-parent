import React from 'react';
import { Box, Typography, Collapse, IconButton, List, ListItemIcon } from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
  UnfoldLess as CollapseAllIcon,
  LightMode as LightModeIcon,
  DarkMode as DarkModeIcon
} from '@mui/icons-material';
import { TreeNode, mockTreeData, ContextMenuData, collapseAll, toggleNode, handleContextMenu } from '../eveli-tree-api';
import { useUtilityClasses, EveliTreeRoot, getIcon, getIconClassName, EveliTreeClasses, StyledListItem, StyledListItemText } from './useUtilityClasses';
import { EveliTreeItemMenu } from './EveliTreeItemMenu';


interface TreeItemProps {
  node: TreeNode;
  level: number;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: TreeNode) => void;
  classes: EveliTreeClasses;
  isDarkTheme: boolean;
}

function sortChildren(children: TreeNode[]) {
  const order = ['article', 'service', 'dialob', 'flow', 'link', 'language', 'printout', 'image', 'template'];
  return children.sort((a, b) => {
    const aIndex = order.indexOf(a.type);
    const bIndex = order.indexOf(b.type);
    return aIndex - bIndex;
  });
}

const TreeItem: React.FC<TreeItemProps> = ({ node, level, onToggle, onContextMenu, classes, isDarkTheme }) => {
  const hasChildren = node.children && node.children.length > 0;

  return (
    <>
      <StyledListItem level={level} isDarkTheme={isDarkTheme} onClick={() => hasChildren && onToggle(node.id)} onContextMenu={(event) => onContextMenu(event, node)}>
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
          {hasChildren ? (
            <IconButton size='small'>
              {node.isExpanded ? <ExpandMoreIcon fontSize='small' className={classes.iconExpand} /> : <ChevronRightIcon fontSize='small' className={classes.iconExpand} />}
            </IconButton>
          ) : (
              <Box sx={{ width: 21, mr: 0.5 }} />
          )}
          <ListItemIcon className={getIconClassName(node, classes)}>
            {getIcon(node)}
          </ListItemIcon>
          <StyledListItemText
            nodeType={node.type}
            nodeName={node.name}
            description={node.description}
            isDarkTheme={isDarkTheme}
          />
        </Box>
      </StyledListItem>
      {hasChildren && (
        <Collapse in={node.isExpanded} timeout={0}>
          <List component='div' disablePadding>
            {sortChildren(node.children || []).map((child) => (
              <TreeItem
                key={child.id}
                node={child}
                level={level + 1}
                onToggle={onToggle}
                onContextMenu={onContextMenu}
                classes={classes}
                isDarkTheme={isDarkTheme}
              />
            ))}
          </List>
        </Collapse>
      )}
    </>
  );
};

export const EveliTree: React.FC = () => {
  const [isDarkTheme, setIsDarkTheme] = React.useState(false);
  const classes = useUtilityClasses(isDarkTheme);
  const [treeData, setTreeData] = React.useState<TreeNode[]>(mockTreeData);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<ContextMenuData | undefined>();


  function handleContextMenuClose() {
    setContextMenuOpen(false);
  }

  return (
    <EveliTreeRoot className={classes.root} isDarkTheme={isDarkTheme}>
      <Box className={classes.title}>
        <Typography className={classes.titleText} mr={3}>Eveli Tree</Typography>
        <Box flexGrow={1} />
        <IconButton size='small' onClick={() => collapseAll(treeData, setTreeData)}
          sx={{
            color: isDarkTheme ? '#cccccc' : '#666666',
            '&:hover': {
              backgroundColor: isDarkTheme ? '#3c3c3c' : '#e0e0e0',
            },
          }}
        >
          <CollapseAllIcon fontSize='small' />
        </IconButton>
        <IconButton size='small' onClick={() => setIsDarkTheme(!isDarkTheme)}
          sx={{
            color: isDarkTheme ? '#cccccc' : '#666666',
            '&:hover': {
              backgroundColor: isDarkTheme ? '#3c3c3c' : '#e0e0e0',
            },
          }}
        >
          {isDarkTheme ? <LightModeIcon fontSize='small' /> : <DarkModeIcon fontSize='small' />}
        </IconButton>
      </Box>
      <List component='nav' disablePadding>
        {treeData.map((node) => (
          <TreeItem
            key={node.id}
            node={node}
            level={0}
            onToggle={(nodeId) => toggleNode(nodeId, treeData, setTreeData)}
            onContextMenu={(event, node) => handleContextMenu(event, node, setContextMenuData, setContextMenuOpen)}
            classes={classes}
            isDarkTheme={isDarkTheme}
          />
        ))}
      </List>

      <EveliTreeItemMenu
        node={contextMenuData?.node || undefined}
        anchorPosition={contextMenuData?.anchorPosition || undefined}
        open={contextMenuOpen}
        onClose={handleContextMenuClose}
        onExited={() => setContextMenuData(undefined)}
      />
    </EveliTreeRoot>
  );
}