import { Button, generateUtilityClass, styled, Typography } from "@mui/material";

import TableChartOutlinedIcon from '@mui/icons-material/TableChartOutlined';
import FilterListOutlinedIcon from '@mui/icons-material/FilterListOutlined';
import composeClasses from "@mui/utils/composeClasses";

const RotatedButton: React.FC<{ label: string, icon: React.ReactNode }> = ({ label, icon }) => {
  const classes = useUtilityClasses();

  return (
    <RotatedButtonsRoot className={classes.root}>
      <Button variant='text' startIcon={icon}>
        <Typography>{label}</Typography>
      </Button>
    </RotatedButtonsRoot>
  )
};


export const VerticalButtonColumn: React.FC = () => {
  const classes = useUtilityClasses();

  return (
    <VerticalButtonsColumnRoot className={classes.root}>
      <RotatedButton label="Columns" icon={<TableChartOutlinedIcon />} />
      <RotatedButton label="Filter" icon={<FilterListOutlinedIcon />} />
    </VerticalButtonsColumnRoot>
  );
};


const MUI_NAME = 'EveliVerticalButtonColumn';
const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

const VerticalButtonsColumnRoot = styled('div', {
  name: MUI_NAME,
  slot: 'VerticalButtonsColumn',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {

  return {
    width: '100%',
    height: '100%',
    grow: 1,
    paddingTop: theme.spacing(1),
    gap: theme.spacing(3),
    display: 'flex',
    borderRadius: '0px 10px 10px 0px',
    backgroundColor: theme.palette.secondary.main,
    border: `1px solid ${theme.palette.divider}`,
    borderLeft: 'unset',
    flexDirection: 'column',
    alignItems: 'center'
  };
});



const RotatedButtonsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'RotatedButtons',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {

  return {
    '.MuiButtonBase-root': {
      writingMode: 'vertical-rl',
      transform: 'rotate(360deg)',
      ':hover': {
        backgroundColor: 'transparent'
      },
      '.MuiButton-icon': {
        marginRight: '0px',
        marginLeft: '0px',
        marginBottom: theme.spacing(1)
      }
    },
    '.MuiTypography-root': {
      color: theme.palette.text.primary,
      fontSize: '10pt'
    }
  }
});