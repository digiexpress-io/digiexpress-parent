import React from 'react';
import { Box, Button, FormControlLabel, Popover, Radio, RadioGroup, Typography } from '@mui/material';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import { FormattedMessage } from 'react-intl';


import { TaskApi } from '@/api-task';


interface CommonProps {
  label: string | React.ReactNode;
  readonly?: boolean;
  messages: Record<string, { id: string; defaultMessage: string }>;
  colorMap: TaskApi.ColorMap; // TODO::: prolly delete whole component 
  invalidValues?: string[];
  value: string | undefined
  handleCallback?: (newValue: string) => void;
}

const getColor = (color: TaskApi.Colors) => {
  switch (color) {
    case TaskApi.Colors.YELLOW: return 'brown';
    case TaskApi.Colors.BLUE: return 'blue';
    case TaskApi.Colors.GREEN: return 'green';
    case TaskApi.Colors.GREY: return 'grey';
    case TaskApi.Colors.RED: return 'red';
    default: return '';
  }
}

const RadioGroupPopover = ({ label, readonly, messages, colorMap, invalidValues, handleCallback, value }: CommonProps) => {

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [radioValue, setRadioValue] = React.useState<string>(value ?? '');

  React.useEffect(() => {
    setRadioValue(value ?? '')
  }, [value]);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
    setRadioValue('');
  };

  const open = Boolean(anchorEl);

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRadioValue(event.target.value);
  };

  const entries = Object.entries(colorMap);

  const handleConfirm = () => {
    if (radioValue) {

      if(handleCallback){
        handleCallback(radioValue);
      }
    }
    setAnchorEl(null);
  };

  return (
    <>
      <Box display="flex" flexDirection="column">
        <Typography>
          {label}
        </Typography>
        <Button
          variant="outlined"
          size='small'
          sx={{ 
            color: getColor(colorMap[value ?? '']), 
            borderColor: getColor(colorMap[value ?? ''] || TaskApi.Colors.BLUE), 
            width: "max-content" , 
            borderRadius: 1, 
            borderWidth: 1,
            padding: "4px 8px",
            textTransform: "uppercase",
            "&:hover": {
                borderWidth: 1,
                borderColor: getColor(colorMap[value ?? ''] || TaskApi.Colors.BLUE),
            }
          }}
          onClick={handleClick}
          endIcon={<ArrowDropDownIcon />}
          disabled={!!readonly}
        >
          {value ? <FormattedMessage {...messages[value]} /> : <FormattedMessage id='button.select' />}
        </Button>
      </Box>
      <Popover
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'center',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'center',
        }}
      >
        <Box sx={{ p: 2 }}>
          <RadioGroup value={radioValue} onChange={handleChange}>
            {entries
              .filter(([value]) => invalidValues ? !invalidValues.includes(value) : true)
              .map(([value, color]) =>
              <FormControlLabel
                key={value}
                value={value}
                control={<Radio />}
                label={<Typography variant='body2'><FormattedMessage {...messages[value]} /></Typography>}
                sx={{ color: getColor(color), textTransform: "uppercase" }}
              />
            )}
          </RadioGroup>
          <Box display='flex' justifyContent='center' mt={2}>
            <Button variant='contained' onClick={handleConfirm} disabled={!radioValue}  ><FormattedMessage id='button.accept'/></Button>
          </Box>
        </Box>
      </Popover>
    </>
  );
}

export default RadioGroupPopover;