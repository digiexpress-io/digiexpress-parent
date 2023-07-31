import React from 'react';
import { Button, TableHead, TableCell, TableRow } from '@mui/material';

import { FormattedMessage } from 'react-intl';
import Styles from '@styles';
import client from '@taskclient';

interface HeadCell {
  id: keyof client.TaskDescriptor;
}

const headCells: readonly HeadCell[] = [
  { id: 'dueDate' },
  { id: 'priority' },
  { id: 'status' },
];


const StyledSpotLight: React.FC<{ value: client.Group }> = ({ value }) => {
  const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset" };
  if (!value) {
    return (<Button color="primary" variant="contained" sx={sx}>Contained</Button>);
  }

  if (value.type === 'myWorkType') {
    const backgroundColor = value.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`core.myWork.myWorkTaskTable.header.spotlight.${value.id}`} />
    </Button>);
  }
  return (<Button color="primary" variant="contained" sx={sx}>
    <FormattedMessage id={`tasktable.header.spotlight.no_group`} />
  </Button>);
}

const DescriptorTableHeader: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  setContent: React.Dispatch<React.SetStateAction<client.TablePagination<client.TaskDescriptor>>>,
  def: client.Group
}> = ({ content, setContent, def }) => {


  return (
    <TableHead>
      <TableRow>
        <TableCell align='left' padding='none'>
          <StyledSpotLight value={def} />
          <Styles.SpotlightLabel values={def.records.length} message='core.teamSpace.taskCount' />
        </TableCell>
        {headCells.map((headCell) => (<Styles.TaskTable.TableHeaderSortable key={headCell.id} id={headCell.id} content={content} setContent={setContent} />))}
      </TableRow>
    </TableHead>
  );
}
//border-top-right-radius
export default DescriptorTableHeader;



