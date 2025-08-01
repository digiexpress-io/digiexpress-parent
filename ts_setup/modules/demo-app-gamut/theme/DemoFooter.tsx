
import React from 'react';
import { Link, Typography, generateUtilityClass, styled } from '@mui/material';
import { useIntl } from 'react-intl';
import { GLogo } from '@dxs-ts/gamut';
import composeClasses from '@mui/utils/composeClasses';

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    spacing: ['spacing'],
    heading: ['heading'],
    links: ['links']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass('DemoFooter', slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const DemoFooter: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <DemoFooterRoot className={classes.root}>
      <div className={classes.spacing}><GLogo variant='black_lg' /></div>
      <div className={classes.spacing}>
        <Typography className={classes.heading}>{intl.formatMessage({ id: 'footer.column2.title' })}</Typography>
        <Typography>{intl.formatMessage({ id: 'footer.column2.subtitle' })}</Typography>
      </div>
      <div className={classes.links}>
        <Typography className={classes.heading}>{intl.formatMessage({ id: 'footer.column3.title' })}</Typography>
        <Link href='https://www.gov.uk/government/publications/sample-accessibility-statement/sample-accessibility-statement-for-a-fictional-public-sector-website'>
          <Typography>{intl.formatMessage({ id: 'footer.column3.link.acessibility' })}</Typography>
        </Link>
        <Link href='https://iapp.org/resources/article/sample-data-protection-policy-template-2/'>
          <Typography>{intl.formatMessage({ id: 'footer.column3.link.dataProtection' })}</Typography>
        </Link>
        <Link href='http://www.google.com'>
          <Typography>{intl.formatMessage({ id: 'footer.column3.link.feedback' })}</Typography>
        </Link>
        <Link href='http://www.google.com'>
          <Typography>{intl.formatMessage({ id: 'footer.column3.link.siteInfo' })}</Typography>
        </Link>
        <Link href='http://www.google.com'>
          <Typography>{intl.formatMessage({ id: 'footer.column3.link.newsletter' })}</Typography>
        </Link>
      </div>
      <div className={classes.spacing}>
        <Typography className={classes.heading}>{intl.formatMessage({ id: 'footer.column4.title' })}</Typography>
        <Typography>{intl.formatMessage({ id: 'footer.column4.email' })}</Typography>
        <Typography>{intl.formatMessage({ id: 'footer.column4.phone' })}</Typography>
        <Typography>{intl.formatMessage({ id: 'footer.column4.address' })}</Typography>
        <Typography>{intl.formatMessage({ id: 'footer.column4.postalCode' })}</Typography>
      </div>
    </DemoFooterRoot>
  )
}


const DemoFooterRoot = styled("div", {
  name: 'DemoFooter',
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.spacing,
      styles.heading,
      styles.links
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    paddingBottom: theme.spacing(3),
    [theme.breakpoints.down('lg')]: {
      flexDirection: 'column',
      fontSize: theme.typography.caption.fontSize,
      textAlign: 'center',
      paddingBottom: theme.spacing(5)
    },
    '& .DemoFooter-heading': {
      fontVariant: theme.typography.h4.fontVariant,
      fontWeight: 'bold',
      color: theme.palette.primary.main,
      marginBottom: theme.spacing(2),
      [theme.breakpoints.down('lg')]: {
        marginTop: theme.spacing(2)
      }
    },
    '& .DemoFooter-links': {
      [theme.breakpoints.up('lg')]: {
        width: '25%',
      },
      [theme.breakpoints.down('md')]: {
        width: '100%',
        textAlign: 'center',
      },
    },
    '& .DemoFooter-spacing': {
      [theme.breakpoints.down('md')]: {
        width: '100%',
        textAlign: 'center'
      },
      [theme.breakpoints.up('lg')]: {
        width: '24%',
      },
    },


  };
});