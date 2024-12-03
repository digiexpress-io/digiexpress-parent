import React from 'react';
import { Box, Chip, Divider, FormControl, FormControlLabel, List, ListItem, ListItemButton, MenuItem, Select, SelectChangeEvent, TextField, Typography } from '@mui/material';
import { Comment, CommentSource } from '../../frontdesk/types/task/Comment';

import * as Burger from '@/burger';

export interface FeedbackAllTasksProps {
  taskId: string | undefined;
  workerReplies: Comment[];
}


// need two filters -- one for category and one for sub-category or type

const categories = [
  'Public pools',
  'Parks and recreation',
  'School services',
  'Community events',
];

const subCategories = [
  'Cleanliness and hygine',
  'Changing facilities',
  'Customer service'
]


export const FeedbackAllTasks: React.FC<FeedbackAllTasksProps> = ({ taskId, workerReplies }) => {
  const [category, setCategory] = React.useState<string[]>([]);
  const [subCategory, setSubCategory] = React.useState<string[]>([]);

  const handleChangeCategory = (event: SelectChangeEvent<typeof category>) => {
    const {
      target: { value },
    } = event;
    setCategory(
      // On autofill we get a stringified value.
      typeof value === 'string' ? value.split(',') : value,
    );
  };

  const handleChangeSubCategory = (event: SelectChangeEvent<typeof category>) => {
    const {
      target: { value },
    } = event;
    setSubCategory(
      // On autofill we get a stringified value.
      typeof value === 'string' ? value.split(',') : value,
    );
  };

  return (
    <div style={{ padding: 10 }}>
      <Typography variant='h1'>All feedback</Typography>

      <Box display='flex' mb={3} gap={1} alignItems='end'>
        <Box width='25%'>
          <Burger.TextField label='feedback.search' onChange={() => { }} value='Enter a feedback id' />
        </Box>

        <Box width='75%'>
          <FormControl sx={{ width: '45%', marginRight: 1 }}>
            <Typography fontWeight='bold'>Filter by Category</Typography>
            <Select
              sx={{ padding: 0 }}
              value={category}
              onChange={handleChangeCategory}
              input={<TextField select sx={{ padding: 0 }} />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((value) => (<>{value}</>))}
                </Box>
              )}
            >
              {categories.map((name) => (
                <MenuItem
                  key={name}
                  value={name}
                >
                  {name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl sx={{ width: '45%' }}>
            <Typography fontWeight='bold'>Filter by Subcategory</Typography>
            <Select
              sx={{ padding: 0 }}
              value={subCategory}
              onChange={handleChangeSubCategory}
              input={<TextField select sx={{ padding: 0 }} />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((value) => (<>{value}</>))}
                </Box>
              )}
            >
              {subCategories.map((name) => (
                <MenuItem
                  key={name}
                  value={name}
                >
                  {name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

        </Box>
      </Box>

      <List dense disablePadding>
        <ListItem dense disableGutters>
          <ListItemButton>
            <Box display='flex' gap={3} width='100%'>
              <Box minWidth='10%' maxWidth='10%'>
                <Typography variant='caption' fontWeight={500}>Feedback id</Typography>
                <Typography sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', display: 'block' }}> 17637480807647-03</Typography>
              </Box>
              <Box width='35%'>
                <Typography variant='caption' fontWeight={500}>Category</Typography>
                <Typography>Parks and recreation</Typography>
              </Box>
              <Box width='35%'>
                <Typography variant='caption' fontWeight={500} >Sub-category</Typography>
                <Typography>Children's playground</Typography>
              </Box>
              <Box>
                <Typography variant='caption' fontWeight={500} >Modified</Typography>
                <Typography>11.12.2024</Typography>
              </Box>
              <Box width='10%'>
                <Typography variant='caption' fontWeight={500} >Published?</Typography>
                <Typography>{workerReplies.length ? 'YES' : 'NO'}</Typography>
              </Box>
              <Box width='5%' height='100%' alignSelf='center'>
                UNPUB
              </Box>
              <Box width='5%' height='100%' alignSelf='center'>
                DELETE
              </Box>
            </Box>
          </ListItemButton>
        </ListItem>

        <Divider />

        <ListItem dense disableGutters>
          <ListItemButton>
            <Box display='flex' gap={3} width='100%'>
              <Box minWidth='10%' maxWidth='10%'>
                <Typography variant='caption' fontWeight={500}>Feedback id</Typography>
                <Typography sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', display: 'block' }}>2</Typography>
              </Box>
              <Box width='35%'>
                <Typography variant='caption' fontWeight={500}>Category</Typography>
                <Typography>Public swimming pools</Typography>
              </Box>
              <Box width='35%'>
                <Typography variant='caption' fontWeight={500}>Sub-category</Typography>
                <Typography>Maintenance and hygeine</Typography>
              </Box>
              <Box>
                <Typography variant='caption' fontWeight={500}>Modified</Typography>
                <Typography>09.12.2024</Typography>
              </Box>
              <Box width='10%'>
                <Typography variant='caption' fontWeight={500}>Published?</Typography>
                <Typography>{workerReplies.length ? 'YES' : 'NO'}</Typography>
              </Box>
              <Box width='5%' height='100%' alignSelf='center'>
                UNPUB
              </Box>
              <Box width='5%' height='100%' alignSelf='center'>
                DELETE
              </Box>
            </Box>
          </ListItemButton>
        </ListItem>

        <Divider />

        <ListItem dense disableGutters>
          <ListItemButton>
            <Box display='flex' gap={3} width='100%'>
              <Box minWidth='10%' maxWidth='10%'>
                <Typography variant='caption' fontWeight={500}>Feedback id</Typography>
                <Typography sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', display: 'block' }}>3172647899</Typography>
              </Box>
              <Box width='35%'>
                <Typography variant='caption' fontWeight={500}>Category</Typography>
                <Typography>Gym rental facilities</Typography>
              </Box>
              <Box width='35%'>
                <Typography variant='caption' fontWeight={500}>Sub-category</Typography>
                <Typography>Quality of services</Typography>
              </Box>
              <Box>
                <Typography variant='caption' fontWeight={500}>Modified</Typography>
                <Typography>03.12.2024</Typography>
              </Box>
              <Box width='10%'>
                <Typography variant='caption' fontWeight={500}>Published?</Typography>
                <Typography>{workerReplies.length ? 'YES' : 'NO'}</Typography>
              </Box>
              <Box width='5%' height='100%' alignSelf='center'>
                UNPUB
              </Box>
              <Box width='5%' height='100%' alignSelf='center'>
                DELETE
              </Box>
            </Box>
          </ListItemButton>
        </ListItem>

        <Divider />

      </List>



    </div >

  )
}