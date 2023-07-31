
import React from 'react';
import { Box } from '@mui/material';
import Client from '@taskclient';


interface TableProps {
  group: Client.Group
}

interface RenderProps extends Client.TableRenderProps<Client.TaskDescriptor>, TableProps { }


interface TableGroupsProps {
  groupBy: Client.GroupBy | undefined,
  orderBy: keyof Client.TaskDescriptor | undefined,

  children: {
    Header: React.ElementType<RenderProps>;
    Rows: React.ElementType<RenderProps>;
    Tools: React.ElementType<{ children: React.ReactNode }> | undefined
  }
}

const Delegate: React.FC<TableGroupsProps> = ({ groupBy, orderBy, children }) => {
  const tasks = Client.useTasks();
  const { loading, state } = tasks;

  const groups = React.useMemo(() => {
    if (groupBy) {
      return state.withGroupBy(groupBy).groups;
    }
    return state.groups;
  }, [state, groupBy]);

  const defaultOrderBy = React.useMemo(() => {
    if (orderBy) {
      return orderBy;
    }
    return "created";
  }, [orderBy]);

  const result = groups.map((group, index) => (
    <React.Fragment key={group.id}>
      {index > 0 ? <Box sx={{ p: 2 }} /> : null}
      <Client.Table<Client.TaskDescriptor, TableProps>
        data={{ loading, records: group.records, defaultOrderBy }}
        render={{
          ext: { group },
          Header: children.Header,
          Rows: children.Rows
        }}
      />
    </React.Fragment>
  ));

  if(children.Tools) {
    return (<children.Tools children={result}/>);
  }



  return (<>{result}</>);
}


const TableGroups: React.FC<TableGroupsProps> = (props) => {
  return (
    <Client.TableProvider>
      <Delegate {...props} />
    </Client.TableProvider>
  );
}





export type { TableProps, RenderProps }
export { TableGroups };


