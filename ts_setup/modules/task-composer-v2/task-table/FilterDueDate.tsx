
import { anyDateFilter, TableDateFilter } from '@dxs-ts/xui-table';
import { FilterFnOption } from '@tanstack/react-table';
import { TaskApi } from '@dxs-ts/task-api';
import { useIntl } from 'react-intl';
import { FormControlLabel, Radio } from '@mui/material';


const OVERDUE_TYPE = 'OVERDUE';

export const filterDueDate: FilterFnOption<TaskApi.Task> = (row, _columnId: string, filterValue: TableDateFilter) => {
  if(filterValue.type as any === OVERDUE_TYPE) {
    const dueDate = row.original.dueDate;
    if(dueDate) {
      return anyDateFilter(dueDate, { type: 'LT', date: filterValue.date });
    }
    return false;
  }
  const latestTagDate = row.original.dueDate;
  return anyDateFilter(latestTagDate, filterValue);
}


const OverdueSelection: React.FC<{ onClick: (date: Date | null) => void }> = ({ onClick }) => {
  const intl = useIntl();
  return (<FormControlLabel 
    value={OVERDUE_TYPE}
    control={<Radio onClick={() => onClick(new Date())} />} 
    label={intl.formatMessage({ id: 'taskTable.col.header.overdue' })} 
  />);
}


function createFilters(): Record<'OVERDUE', React.FC<{ onClick: (date: Date | null) => void }>> {
  const result = {
    'OVERDUE': OverdueSelection
  };
  return result;
}
export const overdueFilter = createFilters();