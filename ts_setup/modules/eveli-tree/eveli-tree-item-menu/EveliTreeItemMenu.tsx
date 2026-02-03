import React from 'react';
import { Box } from '@mui/material';
import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses, EveliTreeItemMenuRoot as Root, MENU_HEIGHT } from './useUtilityClasses';
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

  // Calculate optimal menu positioning based on available space
  const positioningStrategy = React.useMemo(() => {
    if (!props.anchorPosition) {
      return { vertical: 'center' as const, shouldExpandUpward: false };
    }

    const viewportHeight = window.innerHeight;
    const clickY = props.anchorPosition.top;
    const menuHalfHeight = MENU_HEIGHT / 2;

    const spaceAbove = clickY;
    const spaceBelow = viewportHeight - clickY;

    // Check if we can center the menu (enough space above and below)
    const canCenter = spaceAbove >= menuHalfHeight && spaceBelow >= menuHalfHeight;

    let vertical: 'center' | 'top' | 'bottom';
    let shouldExpandUpward: boolean;

    if (canCenter) {
      vertical = 'center';
      shouldExpandUpward = false; // Not relevant for center positioning
    } else {
      // Fall back to up/down positioning based on available space
      shouldExpandUpward = spaceAbove > spaceBelow;
      vertical = shouldExpandUpward ? 'bottom' : 'top';
    }

    // Debug: Remove this once working
    console.log('Smart positioning debug:', {
      vertical,
      shouldExpandUpward,
      canCenter,
      spaceAbove,
      spaceBelow,
      menuHalfHeight
    });

    return { vertical, shouldExpandUpward };
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
      shouldExpandUpward={positioningStrategy.shouldExpandUpward}
      anchorReference="anchorPosition"
      anchorPosition={props.anchorPosition || undefined}
      anchorOrigin={{
        vertical: positioningStrategy.vertical,
        horizontal: 'left',
      }}
      transformOrigin={{
        vertical: positioningStrategy.vertical,
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