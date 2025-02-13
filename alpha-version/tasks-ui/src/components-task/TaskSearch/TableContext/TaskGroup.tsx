import { PreferenceContextType } from 'descriptor-prefs';
import { TaskDescriptor } from 'descriptor-task';
import { ColumnName } from './TaskPrefsContext';
import Table from 'table';



export type TaskPagination = Table.TablePagination<TaskDescriptor>;
export type SetTaskPagination = React.Dispatch<React.SetStateAction<TaskPagination>>;

function getPrefSortGroup(classifierValue: string) {
  return classifierValue + ".";
}
export function getPrefSortId(classifierValue: string, column: ColumnName) {
  return getPrefSortGroup(classifierValue) + column;
}

function taskPaginationPropsAreEqual(prev: TaskPagination, next: TaskPagination): boolean {
  if(prev.src.length != next.src.length) {
    return false;
  }
  if(prev.entries.length != next.entries.length) {
    return false;
  }


  for(let index = 0; index < prev.entries.length; index++) {
    const prevProps = prev.entries[index];
    const nextProps = next.entries[index];

    if(prevProps === undefined && nextProps === undefined) {
      continue;
    }

    if(prevProps === undefined || nextProps === undefined) {
      return false;
    }
    if(!prevProps.equals(nextProps)) {
      return false;
    }
  }
  return true;
}


export function initTaskGroup(classifierValue: string, prefCtx: PreferenceContextType): TaskPagination {
  const prefGroup = getPrefSortGroup(classifierValue);
  const storedPref = prefCtx.pref.sorting.find(({ dataId }) => dataId.startsWith(prefGroup));
  const storedPrefCol = storedPref?.dataId.substring(prefGroup.length) as keyof TaskDescriptor;

  return new Table.TablePaginationImpl<TaskDescriptor>({
    src: [],
    orderBy: storedPrefCol ?? 'dueDate',
    order: storedPref?.direction ?? 'asc',
    sorted: true,
    rowsPerPage: 5,
    propsAreEqual: taskPaginationPropsAreEqual
  })
}