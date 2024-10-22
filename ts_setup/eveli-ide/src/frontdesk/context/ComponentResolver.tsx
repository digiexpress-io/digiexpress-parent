import React from 'react';
import { UserGroup } from '../types/UserGroup';
import { User } from '../types';
import { TaskLinkProps } from '../components/task/TaskLinkComponent';

export interface ComponentResolver {
  taskLinkResolver?: (props:TaskLinkProps)=>JSX.Element|null;
  groupListItemResolver?:(groups:UserGroup[])=>JSX.Element[];
  userListItemResolver?:(users?:User[])=>JSX.Element[];
}