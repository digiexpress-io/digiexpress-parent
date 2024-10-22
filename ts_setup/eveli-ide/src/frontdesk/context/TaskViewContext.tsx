import { User } from "../types";
import { UserGroup } from "../types/UserGroup";
import React from 'react'

type TaskViewConfig = {
  taskUpdateCallback: ()=>void
  users: User[]
  groups: UserGroup[]
  externalThreads?: boolean
  userSelectionFree: boolean
}

export const TaskViewContext = React.createContext<TaskViewConfig>({
  taskUpdateCallback: ()=>{},
  users: [],
  groups: [],
  userSelectionFree: false
});