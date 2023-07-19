import React from 'react';
import { FormattedMessage } from 'react-intl';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import { WorkflowDelete } from '../../workflow/WorkflowDelete';
import { WorkflowEdit } from '../../workflow';
import { StencilClient } from '../../context';
import Burger from '@the-wrench-io/react-burger';

const WorkflowOptions: React.FC<{workflow: StencilClient.Workflow}> = ({ workflow }) => {

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'WorkflowEdit' | 'WorkflowDelete'>(undefined);
  const handleDialogClose = () => setDialogOpen(undefined);

  return (
    <>
      { dialogOpen === 'WorkflowEdit' ? <WorkflowEdit workflowId={workflow.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'WorkflowDelete' ? <WorkflowDelete workflow={workflow} onClose={handleDialogClose} /> : null}

      
      <Burger.TreeItemOption nodeId={workflow.id + 'workflow.edit'}
        icon={EditIcon}
        color='workflow'
        onClick={() => setDialogOpen('WorkflowEdit')}
        labelText={<FormattedMessage id="services.edit" />}>
      </Burger.TreeItemOption>
      
            
      <Burger.TreeItemOption nodeId={workflow.id + 'workflow.delete'}
        icon={DeleteOutlineOutlinedIcon}
        color='workflow'
        onClick={() => setDialogOpen('WorkflowDelete')}
        labelText={<FormattedMessage id="services.delete" />}>
      </Burger.TreeItemOption>
    </>
  );
}

export { WorkflowOptions }
