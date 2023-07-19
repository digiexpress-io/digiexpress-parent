import { styled, Toolbar, ToolbarProps } from '@mui/material';

interface StyledToolbarProps extends ToolbarProps {
  toolbarHeight: number;
}


const StyledToolbar = styled(Toolbar, {
  shouldForwardProp: (prop) => prop !== 'toolbarHeight',
})<StyledToolbarProps>(
  ({ toolbarHeight }) => ({
    height: toolbarHeight,
    flexShrink: 0,
    padding: 0,
    whiteSpace: 'nowrap',
    boxSizing: 'border-box',
  }),
);

export default StyledToolbar;
