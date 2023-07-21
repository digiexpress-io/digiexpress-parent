import React from 'react';


import type { TaskStatus } from '../../taskclient/task-types';
import Burger from '@the-wrench-io/react-burger';
import { useIntl } from 'react-intl';


export const StatusField: React.FC<{selected: TaskStatus, onChange?: (taskStatus: TaskStatus) => void}> = ({selected, onChange}) => {
  const intl = useIntl();

  const items = React.useMemo(() => {
    function createItem(id: TaskStatus): { id: TaskStatus, value: string } {
      return { id, value: intl.formatMessage({ id: `task.fields.status.${id}` }) };
    }
    return [
      createItem("CREATED"),
      createItem("IN_PROGRESS"),
      createItem("COMPLETED"),
      createItem("REJECTED")
    ];
  }, [intl]);


  function handleOnChange(newTaskStatusValue: string | TaskStatus) {
    if(onChange) {
      onChange(newTaskStatusValue as TaskStatus);
    }
  }

  return (
    <Burger.Select label='tasks.status' onChange={handleOnChange} items={items} selected={selected} />
  )
}





