import React from 'react';
import { alpha, Box, styled, SxProps, Typography } from '@mui/material';
import { SimpleTreeView } from '@mui/x-tree-view';

import ChecklistIcon from '@mui/icons-material/Checklist';
import { useNavigate } from 'react-router-dom';

import { useIntl } from 'react-intl';

import * as Burger from '@/burger';
import { MenuItem, MenuItemProps } from './MenuItem';



const iconSize: SxProps = {
  fontSize: '13pt'
}


const menuItems: MenuItemProps[] = [
  { id: 'menu.tasks', to: '/ui/tasks', icon: <ChecklistIcon sx={iconSize} /> },

]

// --------- Frame.tsx ----------
const HOST_URL = process.env.VITE_HOST_URL || 'http://localhost:3000';



const ExplorerSecondaryButtons: React.FC = () => {
  return (
    <Box display='flex' marginTop='auto' justifyContent='center'>
      <Burger.PrimaryButton label='explorer.logout'
        sx={{ width: 350, position: 'fixed', bottom: 0, marginBottom: 3 }}
        onClick={() => window.location.href = `${HOST_URL}/oauth2/authorization/oidcprovider`}
      />
    </Box>

  )
}


export const Explorer: React.FC<{}> = () => {
  const navigate = useNavigate();

  const handleMenuItemClick = (to?: string) => {
    if (to) {
      navigate(to);
    }
  };

  return (<>

    <SimpleTreeView>
      {menuItems.map((item) => (
        <MenuItem
          key={item.id}
          icon={item.icon}
          id={item.id}
          onClick={() => handleMenuItemClick(item.to)}
        />
      )
      )}
    </SimpleTreeView>
  </>
  );
}

const ExplorerTitleBar = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  minWidth: "unset",
  paddingTop: theme.spacing(1.5),
  paddingBottom: theme.spacing(1.5),
  paddingLeft: theme.spacing(2),
  color: theme.palette.explorerItem.dark,
  backgroundColor: alpha(theme.palette.explorerItem.dark, .2),
  '& .MuiTypography-root': {
    marginLeft: theme.spacing(3),
    fontSize: theme.typography.caption.fontSize,
    textTransform: 'uppercase',
  }
}));



export const Secondary: React.FC = () => {
  const intl = useIntl();

  return (<>
    <Box sx={{ backgroundColor: "explorer.main", height: '100%' }}>
      <ExplorerTitleBar>
        <Typography sx={{ color: 'white', fontStyle: 'italic', fontFamily: 'serif' }}>My Logo</Typography>
        <Typography>{intl.formatMessage({ id: 'explorer.title' })}</Typography>
      </ExplorerTitleBar>
      <Box display="flex" flexDirection='column' flexGrow={1}>
        <Explorer />
      </Box>
      <ExplorerSecondaryButtons />
    </Box>
  </>
  )
}

