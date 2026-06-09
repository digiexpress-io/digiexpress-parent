import React from 'react';
import { styled } from '@mui/material';
import  digiExpressLogo from './digi_express_logo.png';

const FsMainDefaultBackgroundRoot = styled('div')(() => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  height: '100%',
  width: '100%',
}));

const FsMainDefaultBackgroundLogo = styled('img')(() => ({
  display: 'block',
  maxWidth: '500px',
  opacity: 0.3,
}));

export const FsMainDefaultBackground: React.FC = () => {
  return (
    <FsMainDefaultBackgroundRoot>
      <FsMainDefaultBackgroundLogo src={digiExpressLogo} alt='DigiExpress' />
    </FsMainDefaultBackgroundRoot>
  );
};
