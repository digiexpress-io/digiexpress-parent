import React from 'react';
import { Box, Typography } from '@mui/material';
import {
  SentimentVerySatisfied as SentimentVerySatisfiedIcon,
  SentimentSatisfied as SentimentSatisfiedIcon,
  SentimentNeutral as SentimentNeutralIcon,
  SentimentDissatisfied as SentimentDissatisfiedIcon,
  SentimentVeryDissatisfied as SentimentVeryDissatisfiedIcon,
} from '@mui/icons-material';

import {
  EveliCustomerRatingsRoot,
  useUtilityClasses,
  getRpsColumnProps,
  getAverageProps,
  RatingKey,
} from './useUtilityClasses';


interface RpsTableCol {
  id: string;
  workflowName: string;
  formName: string;
  rating: number;
}

interface RpsTableRow {
  workflowName: string;
  formName: string;
  count5: number;
  count4: number;
  count3: number;
  count2: number;
  count1: number;
  total: number;
  average: number;
}

interface RpsColumn {
  key: RatingKey;
  Icon: React.ElementType;
}

const COLUMNS: RpsColumn[] = [
  { key: 'count5', Icon: SentimentVerySatisfiedIcon },
  { key: 'count4', Icon: SentimentSatisfiedIcon },
  { key: 'count3', Icon: SentimentNeutralIcon },
  { key: 'count2', Icon: SentimentDissatisfiedIcon },
  { key: 'count1', Icon: SentimentVeryDissatisfiedIcon },
];

function aggregate(rps: RpsTableCol[]): RpsTableRow[] {
  const map = new Map<string, RpsTableRow>();
  for (const entry of rps) {
    const key = `${entry.workflowName}__${entry.formName}`;
    const row = map.get(key) ?? { workflowName: entry.workflowName, formName: entry.formName, count5: 0, count4: 0, count3: 0, count2: 0, count1: 0, total: 0, average: 0 };
    map.set(key, row);
    (row as any)[`count${entry.rating}`]++;
    row.total++;
  }
  return Array.from(map.values())
    .map(row => ({
      ...row,
      average: Math.round(((row.count5 * 5 + row.count4 * 4 + row.count3 * 3 + row.count2 * 2 + row.count1) / row.total) * 100) / 100,
    }))
    .sort((a, b) => b.total - a.total);
}

export const CustomerRatings: React.FC<{ rps: RpsTableCol[] | undefined }> = ({ rps }) => {
  const classes = useUtilityClasses();
  const rows = aggregate(rps ?? []);

  return (
    <EveliCustomerRatingsRoot>
      <Typography className={classes.title}>Customer Ratings</Typography>
      <Box display='flex' flexDirection='column'>

        <Box display='flex' flexDirection='row' className={classes.row}>
          <Box className={classes.nameColumn}>
            <Box className={classes.header}><Typography>Workflow</Typography></Box>
          </Box>
          <Box className={classes.nameColumn}>
            <Box className={classes.header}><Typography>Form</Typography></Box>
          </Box>
          {COLUMNS.map(({ key, Icon }) => (
            <Box key={key} className={classes.columnWrapper}>
              <Box className={classes.header} {...getRpsColumnProps(key)}><Icon /></Box>
            </Box>
          ))}
          <Box className={classes.totalSection}>
            <Box className={classes.header}><Typography>Total</Typography></Box>
          </Box>
          <Box className={classes.totalSection}>
            <Box className={classes.header}><Typography>Average</Typography></Box>
          </Box>
        </Box>

        {rows.map((row, index) => (
          <Box key={index} display='flex' flexDirection='row' className={classes.row}>
            <Box className={classes.nameColumn}>
              <Box className={classes.cell}><Typography className={classes.cellValue}>{row.workflowName}</Typography></Box>
            </Box>
            <Box className={classes.nameColumn}>
              <Box className={classes.cell}><Typography className={classes.cellValue}>{row.formName}</Typography></Box>
            </Box>
            {COLUMNS.map(({ key }) => (
              <Box key={key} className={classes.columnWrapper}>
                <Box className={classes.countsPanel} {...getRpsColumnProps(key)}>
                  <Box className={classes.cell}><Typography className={classes.cellValue}>{row[key]}</Typography></Box>
                </Box>
              </Box>
            ))}
            <Box className={classes.totalSection}>
              <Box className={classes.cell}><Typography className={classes.cellValue}>{row.total}</Typography></Box>
            </Box>
            <Box className={classes.totalSection}>
              <Box className={classes.cell}>
                <Typography className={classes.averageText} {...getAverageProps(row.average)}>
                  {row.average.toFixed(2)}
                </Typography>
              </Box>
            </Box>
          </Box>
        ))}

      </Box>
    </EveliCustomerRatingsRoot>
  );
};
