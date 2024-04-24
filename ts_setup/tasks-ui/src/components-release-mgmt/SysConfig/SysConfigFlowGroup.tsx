import React from 'react';
import { Paper, Box, Stack, Collapse } from '@mui/material';


import Burger from 'components-burger';
import { Group } from 'descriptor-grouping';

import { SysConfigFlow } from './SysConfigFlow';
import { SysConfigForm } from './SysConfigForm';
import { useSysConfig } from '../SysConfigContext';



const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset", fontWeight: 'bolder' };
export const SysConfigFlowGroup: React.FC<{ group: Group }> = ({ group }) => {

  const { sysConfig } = useSysConfig();
  const [formsVisible, setFormsVisible] = React.useState(false);

  function handleFormToggle() {
    setFormsVisible(prev => !prev)
  }

  return (
    <Box sx={{ paddingTop: 1, paddingLeft: 1 }}>
      <Box>
        <Box display="flex">
          <Burger.PrimaryButton sx={sx} label={<>{group.id}</>} onClick={() => { }} />
          <Box flexGrow={1} />
          <Burger.SecondaryButton
            label={formsVisible ? "core.sysconfig.hide" : "core.sysconfig.show"}
            labelValues={{ records: group.value.length }}
            onClick={handleFormToggle} />
        </Box>
        <Paper sx={{ borderTopLeftRadius: '0px', minHeight: '100px' }}>
          <SysConfigFlow flowId={group.id} />
        </Paper>
      </Box>


      <Collapse orientation="vertical" in={formsVisible}>
        <Stack spacing={1}>
          <Box />
          {group.value.map(index => <SysConfigForm index={index} key={index} />)}
        </Stack>
      </Collapse>

    </Box>
  );
}


