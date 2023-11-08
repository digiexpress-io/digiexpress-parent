import {
  PaletteType,
  RoleUnassigned, OwnerUnassigned
} from './types';


export const _nobody_: RoleUnassigned & OwnerUnassigned = '_nobody_';

// https://coolors.co/ff595e-26c485-ffca3a-1982c4-6a4c93
export const bittersweet: string = '#FF595E'; //red
export const emerald: string = '#26C485';     //green
export const sunglow: string = '#FFCA3A';     //yellow
export const steelblue: string = '#1982C4';   //blue
export const ultraviolet: string = '#6A4C93'; //lillac
export const orange: string = '#fb8500';      //orange
export const red: string = '#d00000';

export const Palette: PaletteType = {
  priority: {
    'HIGH': bittersweet,
    'LOW': steelblue,
    'MEDIUM': emerald
  },
  status: {
    'REJECTED': bittersweet,
    'IN_PROGRESS': emerald,
    'COMPLETED': steelblue,
    'CREATED': ultraviolet,
  },
  assigneeGroupType: {
    assigneeOther: steelblue,
    assigneeOverdue: red,
    assigneeStartsToday: orange,
    assigneeCurrentlyWorking: emerald
  },
  teamGroupType: {
    groupOverdue: red,
    groupAvailable: steelblue,
    groupDueSoon: orange
  },
  colors: { red: bittersweet, green: emerald, yellow: sunglow, blue: steelblue, violet: ultraviolet }
}