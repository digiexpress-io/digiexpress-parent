import React from 'react';
import { Box, Typography, IconButton, TextField, Checkbox, Stack, Grid, Divider, Chip, Button, Alert } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import Client from 'client';

import Burger from 'components-burger';

const data = [
  {
    id: "1",
    title: "taskMsgFlow",
    items: [
      {
        id: "",
        status: "OK",
        title: "General message",
        formName: "Yleinen viesti",
        formVersion: "v3.2",
        changed: new Date(),
        changedBy: "olev.mutso@resys.io",
        locales: ["fi", "en"]
      }
    ]
  },
  {
    id: "2",
    title: "taskGenericFlow",
    items: [
      {
        id: "1",
        status: "ERROR",
        title: "Application longer education",
        formName: "Hakemus oikeudesta pidennettyyn oppivelvollisuuteen",
        formVersion: "v2.7",
        changed: new Date(),
        changedBy: "mika.lindholm@resys.io",
        locales: ["fi", "en"]
      },
      {
        id: "2",
        status: "OK",
        title: "Building information",
        formName: "Rakkennustietojen ilmoituslomake",
        formVersion: "v1.4",
        changed: new Date(),
        changedBy: "mika.lindholm@resys.io",
        locales: ["fi", "en"],
        variables: [
          { id: "FirstNames", type: "Text" },
          { id: "LastName", type: "Text" },
          { id: "Address", type: "Text" },
          { id: "SocialSecurityNumber", type: "Text" }
        ]
      },
    ]
  }
];


const ConfigItem: React.FC<{}> = ({ }) => {


  return (<>

    {data.map((checklist) => (
      <Burger.Section key={checklist.id} width='30%'>

        <Box display='flex' alignItems='center' justifyContent='space-between'>
          <Typography fontWeight='bolder'>Wrench flow: {checklist.title}</Typography>

          <Box display='flex' alignItems='center' justifyContent='flex-end' >
            <Button startIcon={<AddIcon sx={{ color: 'uiElements.main' }} />} onClick={() => { }}>
            </Button>
          </Box>
        </Box>

        <Stack spacing={1}>
          <Grid container key={checklist.id} id={checklist.id} direction='row' alignItems='center'
            sx={{
              color: 'text.primary',
              borderRadius: 1,
              width: '100%'
            }}>
          </Grid>

          {/* checklist items section  */}


          {checklist.items.map((item, index) => (<>
            <Alert severity={item.status === 'OK' ? 'success' : 'error'} sx={{ p: 0, pr: 1, m: 0, width: "100%" }} action={
              <>
                <Burger.DateTimeFormatter type='dateTime' value={item.changed} /><Box>&nbsp; {item.changedBy}</Box>
              </>
            }>
              <Typography fontWeight='bolder'>{item.title}</Typography>
            </Alert>
            <Grid container alignItems='center' key={item.id}>
              <Grid item md={3} lg={3}>
                <Typography fontWeight='bolder'>Dialob form</Typography>
              </Grid>
              <Grid item md={9} lg={9} alignItems='center'>
                <Box display='flex' alignItems='center'>
                  {item.formName}/{item.formVersion}
                </Box>
              </Grid>
            </Grid>


            <Grid container>
              <Grid item md={3} lg={3} alignSelf='center'>
                <Typography fontWeight='bolder'>Locales</Typography>
              </Grid>
              <Grid item md={9} lg={9}>
                {item.locales.map(locale => <Chip sx={{ mr: 1 }} label={locale} component="a" href="#basic-chip" clickable />)}
              </Grid>
            </Grid>

            {item.variables &&
              (<Grid container>
                <Grid item md={3} lg={3} alignSelf='center'>
                  <Typography fontWeight='bolder'>Context Variables</Typography>
                </Grid>
                <Grid item md={9} lg={9}>
                  {item.variables.map(variable => <Chip sx={{ mr: 1 }} label={variable.id} component="a" href="#basic-chip" clickable />)}
                </Grid>
              </Grid>)
            }


            {checklist.items.length - 1 === index ? undefined : <Divider />}
          </>

          ))}
        </Stack>

        {/* Add new checklist item  */}

        <Box display='flex' alignItems='center' justifyContent='flex-end' >
          <Button startIcon={<AddIcon sx={{ color: 'uiElements.main' }} />}>
            <Typography sx={{ color: 'text.primary', textTransform: 'capitalize' }}>
              <FormattedMessage id='task.checklistItem.add' />
            </Typography>
          </Button>
        </Box>
      </Burger.Section >
    ))
    }
  </>)
}
export default ConfigItem;