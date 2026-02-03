import React from 'react';
import { Box } from '@mui/material';
import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses, EveliTreeItemMenuRoot as Root } from './useUtilityClasses';
import { EveliTreeItemMenuMain } from './EveliTreeItemMenuMain';
import { EveliTreeItemMenuSub } from './EveliTreeItemMenuSub';

interface EveliTreeItemMenuProps {
  node: TreeNode | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

export const EveliTreeItemMenu: React.FC<EveliTreeItemMenuProps> = (props) => {
  const classes = useUtilityClasses();
  const [openSubmenu, setOpenSubmenu] = React.useState<string | undefined>(undefined);

  // Calculate if menu should expand upward based on available space
  const shouldExpandUpward = React.useMemo(() => {
    if (!props.anchorPosition) {
      return false;
    }
    const viewportHeight = window.innerHeight;
    const clickY = props.anchorPosition.top;
    const spaceBelow = viewportHeight - clickY;

    // Choose the direction with more available space
    const spaceAbove = clickY;
    const shouldExpand = spaceAbove > spaceBelow;

    // Debug: Remove this once working
    console.log('Smart positioning debug:', {
      shouldExpandUpward: shouldExpand,
      spaceAbove,
      spaceBelow
    });

    return shouldExpand;
  }, [props.anchorPosition]);

  React.useEffect(() => {
    if (!props.open) {
      setOpenSubmenu(undefined);
    }
  }, [props.open]);

  function handleSubmenuOpen(submenuType: string) {
    setOpenSubmenu(submenuType);
  }

  return (
    <Root
      className={classes.root}
      open={props.open}
      onClose={props.onClose}
      isSubmenuOpen={!!openSubmenu}
      shouldExpandUpward={shouldExpandUpward}
      anchorReference="anchorPosition"
      anchorPosition={props.anchorPosition || undefined}
      anchorOrigin={{
        vertical: shouldExpandUpward ? 'bottom' : 'top',
        horizontal: 'left',
      }}
      transformOrigin={{
        vertical: shouldExpandUpward ? 'bottom' : 'top',
        horizontal: 'left',
      }}
      slotProps={{
        transition: {
          onExited: props.onExited,
        },
      }}
    >
      <Box className={classes.menuContainer}>
        <EveliTreeItemMenuMain
          node={props.node}
          openSubmenu={openSubmenu}
          onSubmenuOpen={handleSubmenuOpen}
          onClose={props.onClose}
        />
        <EveliTreeItemMenuSub
          node={props.node}
          openSubmenu={openSubmenu}
        />
      </Box>
    </Root>
  );
};