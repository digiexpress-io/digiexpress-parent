import React, { useState } from 'react';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentButtonDeleteProps } from './FsDirentButtonDeleteProps';
import { useUtilityClasses, FsDirentButtonDeleteRoot } from './useUtilityClasses';
import { FsDirentButtonDeleteDialog } from './FsDirentButtonDeleteDialog';

export const FsDirentButtonDelete: React.FC<FsDirentButtonDeleteProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { deleteDirent, getDirent } = useFsDirent();
  const [dialogOpen, setDialogOpen] = useState(false);

  const handleClick = () => setDialogOpen(true);
  const handleClose = () => setDialogOpen(false);
  const handleConfirm = () => {
    setDialogOpen(false);
    const dirent = getDirent(props.assetId);
    if (dirent) {
      deleteDirent(dirent.id, dirent.type);
    }
    props.onDelete?.();
  };

  return (
    <>
      <FsDirentButtonDeleteRoot className={classes.root} onClick={handleClick}>
        {intl.formatMessage({ id: 'button.delete' })}
      </FsDirentButtonDeleteRoot>
      <FsDirentButtonDeleteDialog open={dialogOpen} onClose={handleClose} onConfirm={handleConfirm} assetId={props.assetId}/>
    </>
  );
};
