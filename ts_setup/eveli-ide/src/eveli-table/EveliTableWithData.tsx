import React from 'react';
import { Box } from '@mui/material';
import { EveliTableColRoot, EveliTableHeaderRoot, EveliTableRowRoot } from './useUtilityClasses';

import { EveliTableDrawerButtonColumn } from './EveliTableDrawerButtonColumn';
import { ColSelectItem, EveliTableColSelect } from './EveliTableColSelect';
import { EveliTableDrawerFilters } from './EveliTableDrawerFilters';
import { EveliTableDrawer } from './EveliTableDrawer';


import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';
import { EveliTable, EveliTableCell, EveliTableHeaderCell } from './EveliTable';


export const EveliTableWithData: React.FC = () => {

  const [colsMenuOpen, setColsMenuOpen] = React.useState(false);
  const [filtersMenuOpen, setFiltersMenuOpen] = React.useState(false);

  function toggleColsMenu() {
    setColsMenuOpen(prev => !prev);
    setFiltersMenuOpen(false);
  }

  function toggleFiltersMenu() {
    setFiltersMenuOpen(prev => !prev);
    setColsMenuOpen(false);
  }

  return (

      <EveliTable>
        <EveliTableColRoot width='97%'>
          <EveliTableHeaderRoot>
            <EveliTableHeaderCell children='Priority' filterItems={['value1', 'value2']} />
            <EveliTableHeaderCell children='Name' filterItems={['value1', 'value2']} />
            <EveliTableHeaderCell children='Client' filterItems={['value1', 'value2']} />
            <EveliTableHeaderCell children='Status' filterItems={['value1', 'value2']} />
            <EveliTableHeaderCell children='Assignee' filterItems={['value1', 'value2']} />
            <EveliTableHeaderCell children='Info' filterItems={['value1', 'value2']} />
            <EveliTableHeaderCell children='Due' filterItems={['value1', 'value2']} />
            <EveliTableHeaderCell children='Created' filterItems={['value1', 'value2']} />
          </EveliTableHeaderRoot>

          <EveliTableRowRoot>
            <EveliTableCell children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='Amanda McGibbons' />
            <EveliTableCell children={<IndicatorStatus type='NEW' />} />
            <EveliTableCell children={<IndicatorAssignee name='John Cena' />} />
            <EveliTableCell children='Private data included' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot>
          <EveliTableCell children={<IndicatorPriority type='NORMAL' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='Gerald Bridgerton' />
            <EveliTableCell children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell children={
              <Box display='flex' gap={1}>
                <IndicatorAssignee name='Delana Yankee' />
                <IndicatorAssignee name='George Miller' />
              </Box>
            }
            />
            <EveliTableCell children='' />
            <EveliTableCell children='14.03.2025' />
            <EveliTableCell children='02.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot>
            <EveliTableCell children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell children='School application' />
            <EveliTableCell children='Pamela Anderson' />
            <EveliTableCell children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell children={<IndicatorAssignee name='Tina SuperExtraLongName' />} />
            <EveliTableCell children='Special notes...' />
            <EveliTableCell children='13.03.2025' />
            <EveliTableCell children='05.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot>
            <EveliTableCell children={<IndicatorPriority type='HIGH' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='John Cena' />
            <EveliTableCell children={<IndicatorStatus type='COMPLETED' />} />
            <EveliTableCell children={<IndicatorAssignee name='Alan Wake' />} />
            <EveliTableCell children='Private data included' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot>
            <EveliTableCell children={<IndicatorPriority type='LOW' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='John Cena' />
            <EveliTableCell children={<IndicatorStatus type='COMPLETED' />} />
            <EveliTableCell children={
              <Box display='flex' gap={1}>
                <IndicatorAssignee name='Sylvester Stallone' />
                <IndicatorAssignee name='Anthony Hopkins' />
                <IndicatorAssignee name='Al Pachino' />
              </Box>
            }
            />
            <EveliTableCell children='Private data included' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot>
            <EveliTableCell children={<IndicatorPriority type='LOW' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='Tony McPizza' />
            <EveliTableCell children={<IndicatorStatus type='REJECTED' />} />
            <EveliTableCell children={<IndicatorAssignee name='Jerry Seinfeld' />} />
            <EveliTableCell children='Tough customer...' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>

          <EveliTableRowRoot>
          <EveliTableCell children={<IndicatorPriority type='NORMAL' />} />
            <EveliTableCell children='Send feedback' />
            <EveliTableCell children='John Cena' />
            <EveliTableCell children={<IndicatorStatus type='OPEN' />} />
            <EveliTableCell children={<IndicatorAssignee name='Alan Wake' />} />
            <EveliTableCell children='Private data included' />
            <EveliTableCell children='12.03.2025' />
            <EveliTableCell children='01.03.2025' />
          </EveliTableRowRoot>
        </EveliTableColRoot>

        {colsMenuOpen && <EveliTableDrawer children={<EveliTableColSelect>
          <ColSelectItem colTitle='Priority' />
          <ColSelectItem colTitle='Name' />
          <ColSelectItem colTitle='Client' />
          <ColSelectItem colTitle='Status' />
          <ColSelectItem colTitle='Assignee' />
          <ColSelectItem colTitle='Info' />
          <ColSelectItem colTitle='Due' />
          <ColSelectItem colTitle='Created' />
        </EveliTableColSelect>} />}
        {filtersMenuOpen && <EveliTableDrawer children={<EveliTableDrawerFilters
          status={
            <>
              <IndicatorStatus type='NEW' />
              <IndicatorStatus type='OPEN' />
              <IndicatorStatus type='COMPLETED' />
              <IndicatorStatus type='REJECTED' />
            </>
          }
          priority={
            <>
              <IndicatorPriority type='LOW' />
              <IndicatorPriority type='NORMAL' />
              <IndicatorPriority type='HIGH' />
            </>
          }
        />}
        />
        }

        <EveliTableColRoot width='3%'>
          <EveliTableDrawerButtonColumn onColumnsClick={toggleColsMenu} onFiltersClick={toggleFiltersMenu} />
        </EveliTableColRoot>

    </EveliTable>
  )
}