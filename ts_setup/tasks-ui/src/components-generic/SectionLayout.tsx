import { Typography, Grid } from '@mui/material';
import { FormattedMessage } from 'react-intl';

const SectionLayout: React.FC<{ label: string, value: string | number | undefined }> = ({ label, value }) => {

  return (
    <Grid container>
      <Grid item md={3} lg={3}>
        <Typography fontWeight='bolder'><FormattedMessage id={label} /></Typography>
      </Grid>

      <Grid item md={9} lg={9}>
        <Typography>{value}</Typography>
      </Grid>
    </Grid>
  )
}

export { SectionLayout };