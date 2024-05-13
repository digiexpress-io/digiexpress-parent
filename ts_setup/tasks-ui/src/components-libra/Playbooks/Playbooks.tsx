import React from 'react';
import { XfsBreadcrumbs, XfsFolder, XfsProvider, XfsTree } from 'components-xfile-system';
import { NavigationSticky } from 'components-generic';
import { Box } from '@mui/system';



const Playbooks: React.FC = () => {
  
  return (<>
    <XfsProvider>
      <NavigationSticky>
        <XfsBreadcrumbs />
      </NavigationSticky>

        <Box display='flex'>
          <Box width="50%">
            <XfsTree />
          </Box>
          <Box>
            <XfsTree />
          </Box>
        </Box>
    </XfsProvider>
  </>)
}

export { Playbooks };
