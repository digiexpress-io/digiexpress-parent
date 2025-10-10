import React from 'react';
import { Box } from '@mui/material';


import { TagomiComposerApi } from '@dxs-ts/tagomi-api';
import { ExplorerItem, useTagomiNav } from '../tagomi-nav';
import { ServicesView } from '../tagomi-service';

const root = { height: `100%`, padding: 1, backgroundColor: "primary.contrastText" };

const Main: React.FC<{}> = () => {
  const site = TagomiComposerApi.useSite();
  const { activeItem } = useTagomiNav();

  return React.useMemo(() => {
    if (!site.commitAt) {
      return (<Box>No commits found</Box>);
    }
    if (!activeItem) {
      return (<Box sx={root}></Box>)
    }
    const explorer: ExplorerItem | undefined = activeItem;
    if(!explorer) {
      return (<Box sx={root}></Box>)
    }

    return (<Box sx={root}><ServicesView /></Box>)
  }, [activeItem, site]);
}
export { Main }


