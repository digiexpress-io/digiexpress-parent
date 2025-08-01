import { VersionInfoApi } from './version-info-types'


export const THEMES: Record<'purple' | 'red', VersionInfoApi.ThemeColors> = {
  purple: {
    logo: 'color: #6A0DAD; font-weight: bold; text-shadow: 0 0 5px #6A0DAD;',
    separator: 'color: #00FFFF; font-weight: bold;',
    projectName: 'color: #00FFFF; font-size: 18px; font-weight: bold;',
    info: 'color: #40E0D0; font-style: italic;',
    componentTitle: 'color: #9370DB; font-weight: bold;',
    componentItem: 'color: #DDA0DD; font-style: italic;'
  },
  red: {
    logo: 'color: #DC143C; font-weight: bold; text-shadow: 0 0 10px #DC143C;',
    separator: 'color: #FF6347; font-weight: bold;',
    projectName: 'color: #FF4500; font-size: 18px; font-weight: bold;',
    info: 'color: #FF7F50; font-style: italic;',
    componentTitle: 'color: #CD5C5C; font-weight: bold;',
    componentItem: 'color: #F08080; font-style: italic;'
  }
};


export const LOGOS: Record<string, string> = {
    logo_1_great_ones_wisdom: `       ▄███▄
      ███████
     █████████
    ███▄   ▄███
   ███ ▀███▀ ███
  ███   ███   ███
 ███    ███    ███
███     ███     ███
 ███    ███    ███
  ███   ███   ███
   ███ ▄███▄ ███
    ███▀   ▀███
     █████████
      ███████
       ▀███▀`
  };
