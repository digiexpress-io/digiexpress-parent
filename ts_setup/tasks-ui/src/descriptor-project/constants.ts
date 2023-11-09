import { PaletteType } from './types';
import { Palette as initPalette } from 'descriptor-task';


export const _nobody_ = '_nobody_';

export const Palette: PaletteType = {
  repoType: {
    'DIALOB': initPalette.colors.red,
    'STENCIL': initPalette.colors.green,
    'TASKS': initPalette.colors.blue,
    'WRENCH': initPalette.colors.violet,
  },
  colors: initPalette.colors
}