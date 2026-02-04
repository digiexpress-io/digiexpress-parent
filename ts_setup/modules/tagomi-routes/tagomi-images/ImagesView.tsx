import React from 'react';
import { ImagesViewRoot, useImageUtilityClasses, ImagesList } from '../tagomi-explorer';

export const ImagesView: React.FC = () => {
  const classes = useImageUtilityClasses();
  
  return (
    <ImagesViewRoot className={classes.root}>
      <ImagesList />
    </ImagesViewRoot>)
}


