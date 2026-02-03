import React from 'react';
import { ImagesViewRoot, useUtilityClasses } from '../tagomi-explorer/image/useUtilityClasses';
import { ImagesList } from '../tagomi-explorer/image/ImagesList';

export const ImagesView: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <ImagesViewRoot className={classes.root}>
      <ImagesList />
    </ImagesViewRoot>)
}


