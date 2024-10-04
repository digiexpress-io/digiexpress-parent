import React from 'react';
import { Card, CardContent, Typography, Box, SxProps, IconButton, IconButtonProps, styled, Divider, Grid, CardActions } from '@mui/material';

import Collapse from '@mui/material/Collapse';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { StyledButton } from './StyledButton';


const cardStylesExpanded: SxProps = {
  minWidth: '15%',
  display: 'flex',
  justifyContent: 'space-between',
  flexDirection: 'column',
  height: 'fit-content'
}


interface ExpandMoreProps extends IconButtonProps {
  expand: boolean;
}

const ExpandMore = styled((props: ExpandMoreProps) => {
  const { expand, ...other } = props;
  return <IconButton {...other} />;
})(({ theme, expand }) => ({
  transform: !expand ? 'rotate(0deg)' : 'rotate(180deg)',
  marginLeft: 'auto',
  transition: theme.transitions.create('transform', {
    duration: theme.transitions.duration.shortest,
  }),
}));


interface StyledCardPropsExpanded {
  variant: 'expanded',
  onClick?: () => void,
  content: {
    left?: React.ReactElement,
    center: React.ReactElement,
    right: React.ReactElement
  },
  body: {
    title: React.ReactElement,
    expanded: React.ReactElement
  }  
}

const StyledCardExpanded: React.FC<StyledCardPropsExpanded> = ({ content, body, onClick }) => {
  const [expanded, setExpanded] = React.useState(false);
  const handleExpandClick = () => setExpanded(!expanded)

  return (<Card sx={cardStylesExpanded} raised elevation={10}>
    <CardContent sx={{ padding: 2 }} >
      <Grid container>
        <Grid item xs={2} sm={2} md={2}>{content.left}</Grid>
        <Grid item xs={8} sm={8} md={8} alignSelf='center'><Box display='flex' justifyContent='center'><Typography variant='h4'>{content.center}</Typography></Box></Grid>
        <Grid item xs={2} sm={2} md={2}>{content.right}</Grid>
      </Grid>
    </CardContent>

    <Divider />

    <Box display='flex' sx={{ mr: 1 }}>
      <StyledButton onClick={handleExpandClick} sx={{ width: '100%' }}>
        <Typography variant='h5'>{body.title}</Typography>
      </StyledButton>
      <ExpandMore expand={expanded} aria-expanded={expanded}
        onClick={() => {
          if(onClick) {
            onClick();
          }
          handleExpandClick();
        }}>
        <ExpandMoreIcon />
      </ExpandMore>
    </Box>
    
    <Collapse in={expanded} timeout="auto" unmountOnExit >
      <CardContent sx={{ pl: 2, pr: 2, pt: 1 }}>{body.expanded}</CardContent>
    </Collapse>
  </Card >
  )
}



const cardStylesButtoned: SxProps = {
  minWidth: '15%',
  height: '300px',
  display: 'flex',
  justifyContent: 'space-between',
  flexDirection: 'column'
}


interface StyledCardPropsButtoned {
  variant: 'buttoned',
  content: {
    title: React.ReactElement,
    adornment?: React.ReactElement,
    description: React.ReactElement,
  },
  actions: React.ReactElement,
  onClick?: () => void,
}

const StyledCardButtoned: React.FC<StyledCardPropsButtoned> = ({ content, actions, onClick }) => {

  return (<Card sx={cardStylesButtoned} raised elevation={10} >
    <CardContent sx={{ px: 2, py: 2 }}>
      <Box display='flex' sx={{ height: '5vh' }}>
        <Typography variant='h4' gutterBottom>{content.title}</Typography>
        <Box flexGrow={1} />
        {content.adornment}
      </Box>
      <Typography variant="body1">{content.description}</Typography>
    </CardContent>
    <Box flexGrow={1} />
    <CardActions>
      <StyledButton variant='text' sx={{ width: '100%' }} onClick={onClick}>{actions}</StyledButton>
    </CardActions>
  </Card>
  )
}

type StyledCardProps = StyledCardPropsExpanded | StyledCardPropsButtoned;

const StyledCard: React.FC<StyledCardProps> = (props) => {
  if(props.variant === 'expanded') {
    return <StyledCardExpanded {...props} />;
  } else if(props.variant === 'buttoned') {
    return <StyledCardButtoned {...props} />;
  }
  
  throw new Error(`unknown variant ${JSON.stringify(props)}`)
}

export { StyledCard, cardStylesButtoned }
export type { StyledCardProps }

