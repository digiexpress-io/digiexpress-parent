import React from 'react';
import { FormattedMessage } from 'react-intl';
import { ModeEdit as EditIcon } from '@mui/icons-material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { ScriptEdit } from './ScriptEdit';
import { ScriptDelete } from './ScriptDelete';

interface ScriptOptionsProps {
  scriptId: string;
}

export const ScriptOptions: React.FC<ScriptOptionsProps> = ({ scriptId }) => {
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ScriptEdit' | 'ScriptDelete'>(undefined);
  const handleDialogClose = () => setDialogOpen(undefined);

  return (
    <>
      {dialogOpen === 'ScriptEdit' ? <ScriptEdit scriptId={scriptId} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'ScriptDelete' ? <ScriptDelete scriptId={scriptId} onClose={handleDialogClose} /> : null}

      <Burger.TreeItemOption
        nodeId={scriptId + '-script.edit'}
        color='primary'
        icon={EditIcon}
        onClick={() => setDialogOpen('ScriptEdit')}
        labelText={<FormattedMessage id="tagomi.script.options.edit" />}
      />

      <Burger.TreeItemOption
        nodeId={scriptId + '-script.delete'}
        color='primary'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ScriptDelete')}
        labelText={<FormattedMessage id="tagomi.script.options.delete" />}
      />
    </>
  );
}
