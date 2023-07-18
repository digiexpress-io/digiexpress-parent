import React from 'react';

import { InputBase, Theme, alpha } from '@material-ui/core';
import SearchIcon from '@material-ui/icons/Search';
import { makeStyles } from '@material-ui/styles';
import { useIntl } from 'react-intl';


const useStyles = makeStyles((theme: Theme) => ({
  search: {
    position: 'relative',
    borderRadius: theme.shape.borderRadius,
    backgroundColor: alpha(theme.palette.common.white, 0.15),
    '&:hover': {
      backgroundColor: alpha(theme.palette.common.white, 0.25),
    },
    marginLeft: 0,
    width: '100%',
    [theme.breakpoints.up('sm')]: {
      marginLeft: theme.spacing(1),
      width: 'auto',
    },
  },
  searchIcon: {
    padding: theme.spacing(0, 2),
    height: '100%',
    position: 'absolute',
    pointerEvents: 'none',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  input: {
    color: 'inherit',
    '& .MuiInputBase-input': {
      padding: theme.spacing(1, 1, 1, 0),
      // vertical padding + font size from searchIcon
      paddingLeft: `calc(1em + ${theme.spacing(4)})`,
      transition: theme.transitions.create('width'),
      width: '100%',
      [theme.breakpoints.up('sm')]: {
        width: '12ch',
        '&:focus': {
          width: '20ch',
        },
      },
    },
  },
}));

interface HeaderSearchProps {
  onChange: (value: string, ref: React.RefObject<HTMLDivElement>) => void;
}

const HeaderSearch: React.FC<HeaderSearchProps> = ({ onChange }) => {
  const classes = useStyles();
  const intl = useIntl();
  const ref = React.createRef<HTMLDivElement>();
 
  return (
    <div className={classes.search} ref={ref}>
      <div className={classes.searchIcon}><SearchIcon /></div>
      <InputBase placeholder={intl.formatMessage({id: "search.input.name"})} className={classes.input}
        onChange={({ target }) => onChange(target.value, ref)} />
    </div>
  );
}

export type { HeaderSearchProps };
export { HeaderSearch };
