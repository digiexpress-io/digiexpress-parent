import React from 'react';
import PanoramaFishEyeIcon from '@mui/icons-material/PanoramaFishEye';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { cyan, grey_light } from 'components-colors';


export const FlyoutMenuItemIcon: React.FC<{ children?: boolean | undefined }> = ({ children }) => {
  return (
   <>{children ? <CheckCircleIcon sx={{color: cyan}}/> : <PanoramaFishEyeIcon sx={{color: grey_light}} />}</> 
  );
}
