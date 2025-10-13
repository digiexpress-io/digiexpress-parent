import React from 'react';
import { useTheme } from '@mui/material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { ModeEdit as EditIcon } from '@mui/icons-material';

import { FormattedMessage } from 'react-intl';
import { WorkflowDelete } from '../../stencil-workflow/WorkflowDelete';
import { WorkflowEdit } from '../../stencil-workflow';
import { StencilApi } from '@dxs-ts/stencil-api'
import * as Burger from '@dxs-ts/eveli-primitives';

const WorkflowOptions: React.FC<{workflow: StencilApi.Workflow}> = ({ workflow }) => {
  const theme = useTheme();
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'WorkflowEdit' | 'WorkflowDelete'>(undefined);
  const handleDialogClose = () => setDialogOpen(undefined);

  return (
    <>
      { dialogOpen === 'WorkflowEdit' ? <WorkflowEdit workflowId={workflow.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'WorkflowDelete' ? <WorkflowDelete workflow={workflow} onClose={handleDialogClose} /> : null}

      
      <Burger.TreeItemOption nodeId={workflow.id + 'workflow.edit'}
        icon={EditIcon}
        color={theme.palette.primary.dark}
        onClick={() => setDialogOpen('WorkflowEdit')}
        labelText={<FormattedMessage id="services.edit" />}>
      </Burger.TreeItemOption>
      
            
      <Burger.TreeItemOption nodeId={workflow.id + 'workflow.delete'}
        icon={DeleteOutlineOutlinedIcon}
        color={theme.palette.primary.dark}
        onClick={() => setDialogOpen('WorkflowDelete')}
        labelText={<FormattedMessage id="services.delete" />}>
      </Burger.TreeItemOption>
    </>
  );
}

export { WorkflowOptions }
