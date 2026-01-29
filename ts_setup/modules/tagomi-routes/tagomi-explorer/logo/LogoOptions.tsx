import React from 'react';
import { FormattedMessage } from 'react-intl';
import { ModeEdit as EditIcon } from '@mui/icons-material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { LogoEdit } from '../../tagomi-logos/LogoEdit';
import { LogoDelete } from '../../tagomi-logos/LogoDelete';

interface LogoOptionsProps {
  logoId: string;
}

export const LogoOptions: React.FC<LogoOptionsProps> = ({ logoId }) => {
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'LogoEdit' | 'LogoDelete'>(undefined);
  const handleDialogClose = () => setDialogOpen(undefined);

  return (
    <>
      {dialogOpen === 'LogoEdit' ? <LogoEdit logoId={logoId} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'LogoDelete' ? <LogoDelete logoId={logoId} onClose={handleDialogClose} /> : null}

      <Burger.TreeItemOption
        nodeId={logoId + '-logo.edit'}
        color='primary'
        icon={EditIcon}
        onClick={() => setDialogOpen('LogoEdit')}
        labelText={<FormattedMessage id="tagomi.logo.options.edit" />}
      />

      <Burger.TreeItemOption
        nodeId={logoId + '-logo.delete'}
        color='primary'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('LogoDelete')}
        labelText={<FormattedMessage id="tagomi.logo.options.delete" />}
      />
    </>
  );
}
