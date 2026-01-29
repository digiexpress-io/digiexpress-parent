import React from 'react';
import { Box, Typography, Collapse, IconButton, List, ListItem, ListItemIcon, ListItemText } from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon
} from '@mui/icons-material';
import { TreeNode, mockTreeData } from './mock-tree-data';
import { useUtilityClasses, EveliTreeRoot, getIcon, getTextColor } from './useUtilityClasses';


interface TreeItemProps {
  node: TreeNode;
  level: number;
  onToggle: (nodeId: string) => void;
}

const TreeItem: React.FC<TreeItemProps> = ({ node, level, onToggle }) => {
  const hasChildren = node.children && node.children.length > 0;

  return (
    <Box>
      <ListItem
        sx={{
          pl: level * 1.5,
          py: 0.25,
          minHeight: '24px',
          '&:hover': {
            backgroundColor: '#2d2d30',
          },
          cursor: 'pointer',
        }}
        onClick={() => hasChildren && onToggle(node.id)}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
          {hasChildren ? (
            <IconButton
              size='small'
              sx={{
                p: 0,
                mr: 0.5,
                color: '#cccccc',
                '&:hover': {
                  backgroundColor: 'transparent',
                },
              }}
            >
              {node.isExpanded ? <ExpandMoreIcon fontSize='small' /> : <ChevronRightIcon fontSize='small' />}
            </IconButton>
          ) : (
            <Box sx={{ width: 24, mr: 0.5 }} />
          )}
          <ListItemIcon className={classes.icon} sx={{ color: getTextColor(node) }}>
            {getIcon(node)}
          </ListItemIcon>
          <ListItemText
            primary={
              <Typography variant='body2'
                sx={{
                  color: getTextColor(node),
                  fontSize: '13px',
                  fontWeight: node.type === 'folder' ? 500 : 400,
                }}
              >
                {node.name}
                {node.description && (
                  <Typography
                    component='span'
                    sx={{
                      ml: 1,
                      color: '#6a9955',
                      fontStyle: 'italic',
                    }}
                  >
                    - "{node.description}"
                  </Typography>
                )}
              </Typography>
            }
            sx={{ m: 0 }}
          />
        </Box>
      </ListItem>
      {hasChildren && (
        <Collapse in={node.isExpanded} timeout={0}>
          <List component='div' disablePadding>
            {node.children?.map((child) => (
              <TreeItem
                key={child.id}
                node={child}
                level={level + 1}
                onToggle={onToggle}
              />
            ))}
          </List>
        </Collapse>
      )}
    </Box>
  );
};

export const EveliTree: React.FC = () => {
  const classes = useUtilityClasses();
  const [treeData, setTreeData] = React.useState<TreeNode[]>(mockTreeData);

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

  return (
    <EveliTreeRoot className={classes.root}>
      <Box className={classes.title}>
        <Typography className={classes.titleText}>Eveli Tree</Typography>
      </Box>
      <List component='nav' disablePadding sx={{ p: 0 }}>
        {treeData.map((node) => (
          <TreeItem
            key={node.id}
            node={node}
            level={0}
            onToggle={handleToggle}
          />
        ))}
      </List>
    </EveliTreeRoot>
  );
}