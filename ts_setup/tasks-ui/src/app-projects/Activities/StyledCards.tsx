import React from 'react';
import { Typography, Box, CardHeader, Card, CardContent, Divider, CardActions } from '@mui/material';
import { SxProps } from '@mui/system';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { cyan } from 'components-colors';


interface StyledCardItemProps {
  id: string;
  title: string;
  content: {
    label: string;
    value?: React.ReactNode
  }
  tertiary?: {
    label: string,
    onClick: () => void,
  }
  secondary?: {
    label: string,
    onClick: () => void,
  }
  primary: {
    label: string,
    onClick: () => void,
  }
}
interface StyledCardsProps {
  title: string;
  desc: string;
  items: StyledCardItemProps[];
}


const cardStyle: SxProps = {
  margin: 3,
  width: '20vw',
  display: 'flex',
  flexDirection: 'column',
};

const StyledCardItem: React.FC<StyledCardItemProps> = (props) => {
  const title = (<Box display="flex" sx={{ justifyContent: 'center' }}>
    <Typography variant="h2" sx={{ fontWeight: 'bold', p: 1 }}><FormattedMessage id={props.title} /></Typography>
  </Box>);

  return (<Card sx={cardStyle}>
    <CardHeader sx={{ p: 1, backgroundColor: "table.main" }} title={title} />
    <CardContent sx={{ flexGrow: 1, p: 2, height: 'fit-content' }}>
      <Typography color="mainContent.contrastText" variant="body2"><FormattedMessage id={props.content.label} /></Typography>
      {props.content.value}
    </CardContent>

    <Divider />

    <CardActions sx={{ alignSelf: "flex-end" }}>
      <Box display="flex">
        {props.secondary ? <Burger.SecondaryButton onClick={props.secondary.onClick} label={props.secondary.label} /> : <Box />}
        {props.tertiary ? <Burger.SecondaryButton onClick={props.tertiary.onClick} label={props.tertiary.label} sx={{ color: cyan, alignSelf: 'center' }} /> : null}
        <Burger.PrimaryButton onClick={props.primary.onClick} label={props.primary.label} />
      </Box>
    </CardActions>
  </Card>
  )
}


//card view for all CREATE views
const StyledCards: React.FC<StyledCardsProps> = ({ title, desc, items }) => {

  return (
    <Box>
      <Typography variant="h3" fontWeight="bold" sx={{ p: 2 }}>
        <FormattedMessage id={title} />
        <Typography variant="body2" sx={{ pt: 1 }}><FormattedMessage id={desc} /></Typography>
      </Typography>
      <Box sx={{ margin: 1, display: 'flex', flexWrap: 'wrap', justifyContent: 'center' }}>
        {items.map((card) => (<StyledCardItem key={card.id as string} {...card} />))}
      </Box>
    </Box>
  );
}

export type { StyledCardsProps, StyledCardItemProps };
export { StyledCards };


