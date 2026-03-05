import React from 'react';
import { Box } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { useUtilityClasses, FsDirentMenuRoot as Root, MENU_HEIGHT } from './useUtilityClasses';
import { FsDirentMenuMain } from './FsDirentMenuMain';
import { FsDirentMenuSub } from './FsDirentMenuSub';
import { usePositioningStrategy } from './helpers';

interface FsDirentMenuProps {
  node: FsNode | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

export const FsDirentMenu: React.FC<FsDirentMenuProps> = (props) => {
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
        <FsDirentMenuMain
          node={props.node}
          openSubmenu={openSubmenu}
          onSubmenuOpen={handleSubmenuOpen}
          onClose={props.onClose}
        />

        <FsDirentMenuSub
          node={props.node}
          openSubmenu={openSubmenu}
        />
      </Box>
    </Root>
  );
};