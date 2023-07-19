import React from 'react';
import { ListItemText, Popover, SxProps, Box, Dialog, Typography, Divider, useTheme } from '@mui/material';
import { StyledButton } from './StyledButton';
import { FormattedMessage } from 'react-intl';

export interface SelectProps {
  id: string,
  title?: string,
  variant: 'popover' | 'dialog',
  values: string[],
  selected: string,
  renderValue: (value: string) => React.ReactNode,
  onClick: (newValue: string) => void
}

export const StyledSelect: React.FC<SelectProps> = (props) => {
  if (props.variant === 'popover') {
    return <PopoverSelect {...props} />
  }
  if (!props.title) {
    console.error("title must be defined!");
  }
  return <DialogSelect {...props} />
}

const PopoverSelect: React.FC<SelectProps> = ({ onClick, values, selected, id, renderValue }) => {

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };


  const open = Boolean(anchorEl);

  return (
    <div>
      <StyledButton variant='text' onClick={handleClick}>{renderValue(selected)}</StyledButton>
      <Popover
        id={open ? id : undefined}
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'left',
        }}
      >
        {
          values.map((lang, index) => (
            <ListItemText sx={{ p: 1, m: 1, cursor: 'pointer' }} key={index} onClick={() => {
              onClick(lang);
              handleClose();
            }}>
              {renderValue(lang)}
            </ListItemText>))
        }
      </Popover>
    </div>
  );
}




const titleBar: SxProps = {
  fontWeight: 'bold',
  p: 2,
  alignSelf: 'center'
}

const dialogButton: SxProps = {
  textTransform: 'capitalize',
  color: 'text.primary',
  "&:hover": {
    borderWidth: 0
  },
  "&:active": {
    borderWidth: 0
  }
}

const disabledButton: SxProps = {
  textTransform: 'capitalize',
  "&:disabled": {
    backgroundColor: 'primary.main',
    color: 'secondary.contrastText',
  },
}

const Option: React.FC<{
  key: number,
  value: string,
  selected: string,
  renderValue: (value: string) => React.ReactNode,
  onClick: (newValue: string) => void
}> = ({ key, value, selected, onClick, renderValue }) => {

  const theme = useTheme();
  const disabledStyles = { ...disabledButton, ...{ fontWeight: theme.typography.h1.fontWeight } };

  return (<StyledButton key={key} onClick={() => onClick(value)}
    disabled={value === selected}
    sx={value === selected ? disabledStyles : dialogButton}>
    {renderValue(value)}
  </StyledButton>)
}

const DialogSelect: React.FC<SelectProps> = ({ onClick, values, selected, id, renderValue, title }) => {
  const [open, setOpen] = React.useState(false);

  const options = React.useMemo(() => {
    const handleClick = (value: string) => {
      onClick(value);
      setOpen(false);
    }
    return values.map((lang, key) => (<Option key={key}
      onClick={handleClick}
      value={lang}
      selected={selected}
      renderValue={renderValue} />));
  }, [selected, renderValue, setOpen, onClick]);

  return (
    <Box id={id}>
      <StyledButton variant='outlined' onClick={() => setOpen(true)} sx={{ width: '100%', color: 'text.primary' }}>
        {renderValue(selected)}
      </StyledButton>

      <Dialog fullWidth open={open} onClose={() => setOpen(false)}>
        <Typography sx={titleBar}><FormattedMessage id={title} /></Typography>
        <Divider />
        {options}
      </Dialog>
    </Box>
  );
}


