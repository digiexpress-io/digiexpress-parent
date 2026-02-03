import React from 'react';
import { Typography } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import { ArrowDropDown as ArrowDropDownIcon } from "@mui/icons-material";
import { useIntl } from 'react-intl';

import { ImagesListRoot, useUtilityClasses } from './useUtilityClasses';
import ImageItem from './ImageItem';
import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';


const EndIcon: React.FC = () => {
  return <div style={{ width: 24 }} />;
}

export const ImagesList: React.FC = () => {
  const intl = useIntl();
  const { site } = Composer.useComposer();

  const images = React.useMemo(() => {
    return Object.values(site.resources).filter(r => r.contentType === TagomiApi.ResourceType.IMAGE);
  }, [site.resources]);
  const classes = useUtilityClasses();

  return (
    <ImagesListRoot>
      <Typography className={classes.title}>
        {intl.formatMessage({ id: 'tagomi.main.images.all' })}
      </Typography>

      <SimpleTreeView
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
      >
        {images.map(image => (<ImageItem key={image.id} imageId={image.id} />))}
      </SimpleTreeView>
    </ImagesListRoot>
  );
}
