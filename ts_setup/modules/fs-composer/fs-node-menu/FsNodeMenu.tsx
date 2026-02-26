import React from 'react';
import { Box } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { useUtilityClasses, FsNodeMenuRoot as Root, MENU_HEIGHT } from './useUtilityClasses';
import { FsNodeMenuMain } from './FsNodeMenuMain';
import { FsNodeMenuSub } from './FsNodeMenuSub';
import { usePositioningStrategy } from './helpers';

interface FsNodeMenuProps {
  node: FsNode | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

export const FsNodeMenu: React.FC<FsNodeMenuProps> = (props) => {
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
        <FsNodeMenuMain
          node={props.node}
          openSubmenu={openSubmenu}
          onSubmenuOpen={handleSubmenuOpen}
          onClose={props.onClose}
        />

        <FsNodeMenuSub
          node={props.node}
          openSubmenu={openSubmenu}
        />
      </Box>
    </Root>
  );
};