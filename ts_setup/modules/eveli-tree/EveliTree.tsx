import React from 'react';
import { Box, Typography, Collapse, IconButton, List, ListItemIcon } from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
  UnfoldLess as CollapseAllIcon
} from '@mui/icons-material';
import { TreeNode, mockTreeData } from './mock-tree-data';
import { useUtilityClasses, EveliTreeRoot, getIcon, getIconClassName, EveliTreeClasses, StyledListItem, StyledListItemText } from './useUtilityClasses';
import { EveliTreeItemMenu } from './EveliTreeItemMenu';


interface TreeItemProps {
  node: TreeNode;
  level: number;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: TreeNode) => void;
  classes: EveliTreeClasses;
}

function sortChildren(children: TreeNode[]) {
  const order = ['article', 'service', 'form', 'flow'];
  return children.sort((a, b) => {
    const aIndex = order.indexOf(a.type);
    const bIndex = order.indexOf(b.type);
    return aIndex - bIndex;
  });
}

const TreeItem: React.FC<TreeItemProps> = ({ node, level, onToggle, onContextMenu, classes }) => {
  const hasChildren = node.children && node.children.length > 0;

  return (
    <>
      <StyledListItem
        level={level}
        onClick={() => hasChildren && onToggle(node.id)}
        onContextMenu={(event) => onContextMenu(event, node)}
      >
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
              />
            ))}
          </List>
        </Collapse>
      )}
    </>
  );
};

export const EveliTree: React.FC = () => {
  const classes = useUtilityClasses();
  const [treeData, setTreeData] = React.useState<TreeNode[]>(mockTreeData);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<{
    node: TreeNode;
    anchorPosition: { top: number; left: number };
  } | undefined>();

  const collapseAll = () => {
    const collapseNode = (nodes: TreeNode[]): TreeNode[] => {
      return nodes.map((node) => ({
        ...node,
        isExpanded: false,
        children: node.children ? collapseNode(node.children) : undefined,
      }));
    };
    setTreeData(collapseNode(treeData));
  };

  const handleToggle = (nodeId: string) => {
    const updateNode = (nodes: TreeNode[]): TreeNode[] => {
      return nodes.map((node) => {
        if (node.id === nodeId) {
          return { ...node, isExpanded: !node.isExpanded };
        }
        if (node.children) {
          return { ...node, children: updateNode(node.children) };
        }
        return node;
      });
    };

    setTreeData(updateNode(treeData));
  };

  function handleContextMenu(event: React.MouseEvent, node: TreeNode) {
    event.preventDefault();
    setContextMenuData({
      node,
      anchorPosition: {
        top: event.clientY,
        left: event.clientX,
      },
    });
    setContextMenuOpen(true);
  }

  function handleContextMenuClose() {
    setContextMenuOpen(false);
  }

  return (
    <EveliTreeRoot className={classes.root}>
      <Box className={classes.title}>
        <Typography className={classes.titleText} mr={3}>Eveli Tree</Typography>
        <IconButton size='small' onClick={collapseAll}
          sx={{
            color: '#cccccc',
            '&:hover': {
              backgroundColor: '#3c3c3c',
            },
          }}
        >
          <CollapseAllIcon fontSize='small' />
        </IconButton>
      </Box>
      <List component='nav' disablePadding>
        {treeData.map((node) => (
          <TreeItem
            key={node.id}
            node={node}
            level={0}
            onToggle={handleToggle}
            onContextMenu={handleContextMenu}
            classes={classes}
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