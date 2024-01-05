import { bittersweet, emerald, orange, red, steelblue, ultraviolet } from 'components-colors';

import {
  PaletteType,
  RoleUnassigned, OwnerUnassigned
} from './types';


export const _nobody_: RoleUnassigned & OwnerUnassigned = '_nobody_';


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
}