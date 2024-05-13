import React from 'react';
import { XFsBreadcrumbs, XFsFolder, XFsProvider, XFsTree } from 'components-xfile-system';
import { NavigationSticky } from 'components-generic';
import { Box } from '@mui/system';



const Playbooks: React.FC = () => {
  
  return (<>
    <FileSystemProvider>
      <NavigationSticky>
        <FileSystemBreadcrumbs />
      </NavigationSticky>

        <Box display='flex'>
          <Box width="50%">
            <FileSystemTree />
          </Box>
          <Box>
            <FileSystemFolder />
          </Box>
        </Box>
    </FileSystemProvider>
  </>)
}

export { Playbooks };
