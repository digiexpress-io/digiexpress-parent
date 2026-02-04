import React from 'react';
import { FormattedMessage } from 'react-intl';
import { ModeEdit as EditIcon } from '@mui/icons-material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { ImageEdit, ImageDelete } from '../../tagomi-images';

interface ImageOptionsProps {
  imageId: string;
}

export const ImageOptions: React.FC<ImageOptionsProps> = ({ imageId }) => {
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ImageEdit' | 'ImageDelete'>(undefined);
  const handleDialogClose = () => setDialogOpen(undefined);

  return (
    <>
      {dialogOpen === 'ImageEdit' ? <ImageEdit imageId={imageId} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'ImageDelete' ? <ImageDelete imageId={imageId} onClose={handleDialogClose} /> : null}

      <Burger.TreeItemOption
        nodeId={imageId + '-image.edit'}
        color='primary'
        icon={EditIcon}
        onClick={() => setDialogOpen('ImageEdit')}
        labelText={<FormattedMessage id="tagomi.image.options.edit" />}
      />

      <Burger.TreeItemOption
        nodeId={imageId + '-image.delete'}
        color='primary'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ImageDelete')}
        labelText={<FormattedMessage id="tagomi.image.options.delete" />}
      />
    </>
  );
}
