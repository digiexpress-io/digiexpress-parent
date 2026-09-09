import React from 'react';
import { Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import {
  SentimentVerySatisfied as SentimentVerySatisfiedIcon,
  SentimentSatisfied as SentimentSatisfiedIcon,
  SentimentNeutral as SentimentNeutralIcon,
  SentimentDissatisfied as SentimentDissatisfiedIcon,
  SentimentVeryDissatisfied as SentimentVeryDissatisfiedIcon,
} from '@mui/icons-material';

import {
  EveliCustomerRatingsRoot,
  EveliCustomerRatingsTitle,
  EveliCustomerRatingsHeaderText,
  EveliCustomerRatingsBodyText,
  EveliCustomerRatingsIconCell,
  EveliCustomerRatingsColorCell,
  averageBg,
  getRpsColumnBg,
  getRpsColumnIconColor,
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

type RatingKey = 'count5' | 'count4' | 'count3' | 'count2' | 'count1';

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
    const row = map.get(key) ?? {
      workflowName: entry.workflowName,
      formName: entry.formName,
      count5: 0,
      count4: 0,
      count3: 0,
      count2: 0,
      count1: 0,
      total: 0,
      average: 0
    };
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
  const theme = useTheme();
  const rows = aggregate(rps ?? []);
  const bg = getRpsColumnBg(theme);
  const iconColor = getRpsColumnIconColor(theme);

  return (
    <EveliCustomerRatingsRoot>
      <EveliCustomerRatingsTitle>Customer Ratings</EveliCustomerRatingsTitle>
      <Table size='small'>
        <TableHead>
          <TableRow>
            <TableCell><EveliCustomerRatingsHeaderText>Workflow</EveliCustomerRatingsHeaderText></TableCell>
            <TableCell><EveliCustomerRatingsHeaderText>Form</EveliCustomerRatingsHeaderText></TableCell>
            {COLUMNS.map(({ key, Icon }) => (
              <EveliCustomerRatingsColorCell key={key} align='center' bgColor={bg[key]}>
                <EveliCustomerRatingsIconCell iconColor={iconColor[key]}>
                  <Icon />
                </EveliCustomerRatingsIconCell>
              </EveliCustomerRatingsColorCell>
            ))}
            <TableCell align='center'><EveliCustomerRatingsHeaderText>Total</EveliCustomerRatingsHeaderText></TableCell>
            <TableCell align='center'><EveliCustomerRatingsHeaderText>Average</EveliCustomerRatingsHeaderText></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={`${row.workflowName}__${row.formName}`}>
              <TableCell><EveliCustomerRatingsBodyText>{row.workflowName}</EveliCustomerRatingsBodyText></TableCell>
              <TableCell><EveliCustomerRatingsBodyText>{row.formName}</EveliCustomerRatingsBodyText></TableCell>
              {COLUMNS.map(({ key }) => (
                <EveliCustomerRatingsColorCell key={key} align='center' bgColor={bg[key]}>
                  <EveliCustomerRatingsBodyText>{row[key]}</EveliCustomerRatingsBodyText>
                </EveliCustomerRatingsColorCell>
              ))}
              <TableCell align='center'><EveliCustomerRatingsBodyText>{row.total}</EveliCustomerRatingsBodyText></TableCell>
              <EveliCustomerRatingsColorCell align='center' bgColor={averageBg(theme, row.average)}>
                <EveliCustomerRatingsBodyText>{row.average.toFixed(2)}</EveliCustomerRatingsBodyText>
              </EveliCustomerRatingsColorCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </EveliCustomerRatingsRoot>
  );
};
