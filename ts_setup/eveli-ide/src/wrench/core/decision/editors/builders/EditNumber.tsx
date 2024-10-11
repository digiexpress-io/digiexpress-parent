/*-
 * #%L
 * wrench-assets-ide
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import React from 'react'

import { Button, Box, Grid, ListItemText } from '@mui/material';
import * as Burger from '@/burger';
import { FormattedMessage } from 'react-intl'
import { NumberBuilder } from './'


export const EditNumber: React.FC<{ builder: NumberBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  const handleComparisonTypeChange = (type: string) => {
    if (type === builder.getType()) {
      return;
    }
    onChange(builder.withType(type))
  }

  const handleComparisonOperatorChange = (value: string) => {
    onChange(builder.withOperator(value))
  }

  const handleComparisonValueChange = (value: number) => {
    onChange(builder.withOperatorValue(value + ''))
  }

  const handleRangeStartChange = (value: number) => {
    onChange(builder.withStart(value + ''))
  }

  const handleRangeEndChange = (value: number) => {
    onChange(builder.withEnd(value + ''))
  }

  const handleRangeStartIncludeChange = (value: boolean) => {
    onChange(builder.withStartInclude(value))
  }

  const handleRangeEndIncludeChange = (value: boolean) => {
    onChange(builder.withEndInclude(value))
  }

  const comparisonRenderer = () => {

    return <Box component="form" noValidate autoComplete="off">
      <Box display="flex" sx={{ pb: 1 }}>
        <Box flexGrow={1} />
        <Burger.PrimaryButton
          label="decisions.cells.newvalue.number.comparisonToRange"
          onClick={() => handleComparisonTypeChange('range')} />
      </Box>
      <Grid container spacing={2}>
        <Grid item xs={3}>
          <Burger.Select
            label='decisions.cells.newvalue.number.comparisonType'
            onChange={handleComparisonOperatorChange}
            selected={builder.getOperator()}
            empty={{ id: '', label: 'decisions.cells.newvalue.number.comparisonTypeEmpty' }}
            items={builder.comparisonOperators.map((v) => ({
              id: v.value,
              value: (<ListItemText primary={v.text} />)
            }))}
          />
        </Grid>
        <Grid item xs={9}>
          <Burger.NumberField
            label='decisions.cells.newvalue.number.comparisonValue'
            value={builder.getValue() as any}
            onChange={handleComparisonValueChange} />
        </Grid>
      </Grid>
    </Box>
  }

  const rangeRenderer = () => {

    return <Box component="form" noValidate autoComplete="off">
      <Box display="flex" sx={{ pb: 1 }}>
        <Box flexGrow={1} />
        <Button variant="outlined" onClick={() => handleComparisonTypeChange('operator')}><FormattedMessage id='dt.cell.change.comparison' /></Button>
      </Box>
      <Grid container spacing={2}>
        <Grid item xs={9}>
          <Burger.NumberField label="decisions.cells.newvalue.number.rangeStart"
            value={builder.getStart() as any}
            onChange={newValue => handleRangeStartChange(newValue)} />
        </Grid>
        <Grid item xs={3}>
          <Burger.Switch
            checked={builder.isStart()}
            label="decisions.cells.newvalue.number.rangeInclude"
            onChange={handleRangeStartIncludeChange} />
        </Grid>
        <Grid item xs={9}>
          <Burger.NumberField 
            label="decisions.cells.newvalue.number.rangeEnd"
            value={builder.getEnd() as any}
            onChange={handleRangeEndChange} />
        </Grid>
        <Grid item xs={3}>
          <Burger.Switch
            checked={builder.isEnd()}
            label="decisions.cells.newvalue.number.rangeInclude"
            onChange={handleRangeEndIncludeChange}/>
        </Grid>
      </Grid>
    </Box>
  }


  return builder.getType() === 'range' ? rangeRenderer() : comparisonRenderer()
}
