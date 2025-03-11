
import { IamApi } from '@/burger'
import React from 'react'

type TaskViewConfig = {
  taskUpdateCallback: ()=>void
  users: IamApi.User[]
  groups: IamApi.UserGroup[]
  externalThreads?: boolean
  userSelectionFree: boolean
}

export const TaskViewContext = React.createContext<TaskViewConfig>({
  taskUpdateCallback: ()=>{},
  users: [],
  groups: [],
  userSelectionFree: false
});