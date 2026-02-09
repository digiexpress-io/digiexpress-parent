import React from 'react';
import { Box } from '@mui/material';
import { TreeNode } from '../../eveli-tree-api';
import { useUtilityClasses, EveliTreeItemMenuRoot as Root, MENU_HEIGHT } from './useUtilityClasses';
import { EveliTreeItemMenuMain } from './EveliTreeItemMenuMain';
import { EveliTreeItemMenuSub } from './EveliTreeItemMenuSub';
import { usePositioningStrategy } from './helpers';

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
  const { shouldExpandUpward, vertical } = usePositioningStrategy(props.anchorPosition, MENU_HEIGHT);

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
        vertical: vertical,
        horizontal: 'left',
      }}
      transformOrigin={{
        vertical: vertical,
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