import React from 'react';
import { FormattedMessage } from 'react-intl';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Popover, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core';


import { Layout, Hdes } from '../deps';
import Resource from '../';
import Title from './Title';
import CreateTable from './CreateTable';
import Summary from './Summary';


const useStyles = makeStyles((_theme: Theme) =>
  createStyles({
    root: {
      overflow: 'auto',
      height: '100%'
    },
    popover: {
      pointerEvents: 'none',
    },
    paper: {
      height: '500px',
      width: '600px',
      overflow: 'auto'
    },
  }),
);



interface SearchProps {
  children?: string;
};

const Search: React.FC<SearchProps> = ({ children }) => {
  const classes = useStyles();

  const resource = Resource.useContext();
  const layout = Layout.useContext();

  const [fl, setFl] = React.useState<Hdes.ModelAPI.Model[]>([]);
  const [ft, setFt] = React.useState<Hdes.ModelAPI.Model[]>([]);
  const [dt, setDt] = React.useState<Hdes.ModelAPI.Model[]>([]);
  const [tg, setTg] = React.useState<Hdes.ModelAPI.Model[]>([]);
  
  const [pinned, setPinned] = React.useState<boolean>(false);
  const [openPopoverRow, setOpenPopoverRow] = React.useState<string | null>();
  const [anchorEl, setAnchorEl] = React.useState<HTMLElement | null>(null);
  
  const handlePopoverOpen = (event: any, id: string) => {
    if(openPopoverRow !== id && !pinned) {
      setOpenPopoverRow(id);
      setAnchorEl(event.currentTarget);
    }
  }
  
  const handlePopoverClose = () => {
    if(!pinned) {
      setAnchorEl(null);
      setOpenPopoverRow(null)
    } 
  }
  
  const openPopover = Boolean(anchorEl);
  const handleOpen = (id: string) => {
    const model = resource.session.getModel(id);
    layout.actions.handleTabAdd({id: model.id, label: model.name});
  };

  React.useEffect(() => {
    const models = resource.session.models;
    const keyword = children ? children.toLocaleLowerCase() : '';
    if(keyword.length < 1) {
      return;
    }
    const matcher = (model: Hdes.ModelAPI.Model): boolean => {
      return model.name.indexOf(keyword) > -1;
    };
    
    setFl(models.FLOW.filter(matcher));
    setFt(models.FLOW_TASK.filter(matcher));
    setDt(models.DT.filter(matcher));
    setTg(models.TAG.filter(matcher));

  }, [children, setFl, setDt, setFt, setTg, resource.session.models])

  return (<div className={classes.root}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell></TableCell>
            <TableCell></TableCell>
            <TableCell><FormattedMessage id={'search.header.name'} /></TableCell>
            <TableCell><FormattedMessage id={'search.header.created.modified'} /></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow>
            <TableCell colSpan={4}>
              <Title disabled={fl.length === 0}><FormattedMessage id='search.groups.found.fl' values={{ size: fl.length }} /></Title>
            </TableCell>
          </TableRow>
          <CreateTable children={fl}
            pinned={pinned}
            onOpen={handleOpen}
            openPopover={openPopover} 
            onMouseClick={(event, id) => setPinned(!pinned)}
            onMouseEnter={(event, id) => handlePopoverOpen(event, id)} 
            onMouseLeave={handlePopoverClose} 
          />
          <TableRow>
            <TableCell colSpan={4}>
              <Title disabled={ft.length === 0}><FormattedMessage id='search.groups.found.ft' values={{ size: ft.length }} /></Title>
            </TableCell>
          </TableRow>
          <CreateTable children={ft}
            pinned={pinned}
            onOpen={handleOpen}
            openPopover={openPopover} 
            onMouseClick={(event, id) => setPinned(!pinned)}
            onMouseEnter={(event, id) => handlePopoverOpen(event, id)} 
            onMouseLeave={handlePopoverClose} 
          />
          <TableRow>
            <TableCell colSpan={4}>
              <Title disabled={dt.length === 0}><FormattedMessage id='search.groups.found.dt' values={{ size: dt.length }} /></Title>
            </TableCell>
          </TableRow>
          <CreateTable children={dt}
            pinned={pinned}
            onOpen={handleOpen}
            openPopover={openPopover}
            onMouseClick={(event, id) => setPinned(!pinned)}
            onMouseEnter={(event, id) => handlePopoverOpen(event, id)} 
            onMouseLeave={handlePopoverClose} 
          />
          <TableRow>
            <TableCell colSpan={4}>
              <Title disabled={tg.length === 0}><FormattedMessage id='search.groups.found.tg' values={{ size: tg.length }} /></Title>
            </TableCell>
          </TableRow>
          <CreateTable children={tg}
            pinned={pinned}
            onOpen={handleOpen}
            openPopover={openPopover} 
            onMouseClick={(event, id) => setPinned(!pinned)}
            onMouseEnter={(event, id) => handlePopoverOpen(event, id)} 
            onMouseLeave={handlePopoverClose} 
          />
        </TableBody>
      </Table>
      
      <Popover id="mouse-over-popover"
        className={classes.popover}
        classes={{ paper: classes.paper }}
        open={openPopover}
        anchorEl={anchorEl}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
        transformOrigin={{ vertical: 'top', horizontal: 'left' }}
        onClose={handlePopoverClose}
        disableRestoreFocus>
        {openPopoverRow ? <Summary id={openPopoverRow} /> : null}
      </Popover>

  </div>);
}

export type { SearchProps };
export { Search };
