import React from 'react';
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, List, Paper, ButtonGroup, Button } from '@material-ui/core';

import { Layout, Hdes } from '../deps';
import Resource from '../'
import ExplorerAPI, { filters } from './ExplorerAPI';
import Group from './Group';


const useStyles = (props: { width: number, height: number }) => makeStyles((theme: Theme) =>
  createStyles({
    root: {
      width: '100%',
      height: `${props.height}px`,
      overflow: 'auto',
      backgroundColor: theme.palette.background.default,
    },
    subheader: {
      paddingLeft: theme.spacing(1),
    },
    nested: {
      paddingLeft: theme.spacing(4),
    }
  }),
)();

const Explorer: React.FC<{}> = () => {
  const { session } = Resource.useContext();
  const layout = Layout.useContext();
  const classes = useStyles(layout.session.dimensions);
  const [serviceType, setServiceType] = React.useState<Hdes.ModelAPI.ServiceType>();
  const [modes, setModes] = React.useState<ExplorerAPI.FilterType[]>([]);
  const models = session.models;

  const handleFilter = (filter: ExplorerAPI.FilterDef) => {
    if (filter.serviceType) {
      if (filter.serviceType === serviceType) {
        setServiceType(undefined);
      } else {
        setServiceType(filter.serviceType);
      }
    } else {
      const index = modes.indexOf(filter.value, 0);
      const newSearch = [...modes];
      if (index > -1) {
        newSearch.splice(index, 1);
      } else {
        newSearch.push(filter.value);
      }
      setModes(newSearch);
    }
  };


  let groups;
  if (serviceType) {
    const group = models[serviceType];
    groups = (<Group key={serviceType} id={serviceType} models={group} type={serviceType} modes={modes} label={false}/>);
  } else {
    groups = Object.values(filters).map(filter => {
      if (!filter.serviceType) {
        return (null)
      }
      const group = models[filter.serviceType];
      return (<Group key={filter.id} id={filter.id} models={group} type={filter.serviceType} modes={modes} label={true}/>);
    })
  }

  return (
    <Paper className={classes.root} elevation={3}>
      <ButtonGroup color="primary" fullWidth>
        {Object.values(filters).map((filter) => (
          <Button key={filter.id}
            color={(
              (serviceType === filter.serviceType && filter.serviceType) || modes.includes(filter.value)
            ) ? "secondary" : "primary"}
            onClick={() => handleFilter(filter)}
          >
            {filter.value}
          </Button>
        ))}
      </ButtonGroup>
      <List dense disablePadding component="nav">
        {groups}
      </List>
    </Paper>);
}

export default Explorer;

