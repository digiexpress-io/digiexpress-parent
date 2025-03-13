import React from 'react';

import { TaskLinkProps } from '../components/task/TaskLinkComponent';
import { IamApi } from '@/burger';

export interface ComponentResolver {
  taskLinkResolver?: (props:TaskLinkProps)=>JSX.Element|null;
  groupListItemResolver?:(groups: IamApi.UserGroup[])=>JSX.Element[];
  userListItemResolver?:(users?: IamApi.User[])=>JSX.Element[];
}