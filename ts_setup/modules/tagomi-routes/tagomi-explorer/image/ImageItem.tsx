import React from "react";
import { Box } from '@mui/material';
import { Image as ImageIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

import { TreeItem } from "@dxs-ts/eveli-primitives";
import { ImageOptions } from './ImageOptions';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


interface ImageItemProps {
  imageId: string;
}

const ImageItem: React.FC<ImageItemProps> = ({ imageId }) => {
  const { site } = Composer.useComposer();
  const image = Object.values(site.resources).find(r => r.id === imageId);

  if (!image) {
    return null;
  }

  const imageName = image.resourceName;

  return (
    <>
      <TreeItem
        itemId={imageId}
        labelcolor="explorerItem"
        labelIcon={ImageIcon}
        labelText={
          <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
            {imageName}
          </Box>
        }
      >
        <TreeItem itemId={imageId + '-options-nested'} labelText={<FormattedMessage id="options" />}>
          <ImageOptions imageId={imageId} />
        </TreeItem>
      </TreeItem>
    </>
  );
}

export default ImageItem;
