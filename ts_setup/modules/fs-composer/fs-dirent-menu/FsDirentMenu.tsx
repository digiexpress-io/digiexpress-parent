import React from 'react';
import { Box } from '@mui/material';
import { FsDirent, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses, FsDirentMenuRoot as Root, MENU_HEIGHT } from './useUtilityClasses';
import { FsDirentMenuMain } from './FsDirentMenuMain';
import { FsDirentMenuSub } from './FsDirentMenuSub';
import { usePositioningStrategy } from './helpers';

interface FsDirentMenuProps {
  dirent: FsDirent.Dirent | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

export const FsDirentMenu: React.FC<FsDirentMenuProps> = (props) => {
  const classes = useUtilityClasses();
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();
  const { shouldExpandUpward, vertical } = usePositioningStrategy(props.anchorPosition, MENU_HEIGHT);
  const direntEntry = props.dirent ? getDirent(props.dirent.id) : undefined;
  const [openSubmenu, setOpenSubmenu] = React.useState<string | undefined>(undefined);

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
      isDarkMode={isDarkMode}
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
          dirent={direntEntry}
          openSubmenu={openSubmenu}
          onSubmenuOpen={handleSubmenuOpen}
          onClose={props.onClose}
        />

        <FsDirentMenuSub
          dirent={direntEntry}
          openSubmenu={openSubmenu}
          onClose={props.onClose}
        />
      </Box>
    </Root>
  );
};