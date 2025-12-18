import React from 'react';
import { Grid2, Typography } from '@mui/material';
import numbro from "numbro";


const DEFAULT_FORMAT: numbro.Format = {
  thousandSeparated: true,
  mantissa: 2,
  currencySymbol: " €",
  currencyPosition: "postfix",
}

numbro.registerLanguage({
  languageTag: "fi-FI",
  delimiters: { thousands: ".", decimal: "," },
  abbreviations: { thousand: "k", million: "m", billion: "b", trillion: "t" },
  ordinal: () => ".",
  currency: { symbol: " €", position: "postfix", code: "EUR" },
  formats: {
    fourDigits: {},
    fullWithTwoDecimals: {},
    fullWithTwoDecimalsNoCurrency: {},
    fullWithNoDecimals: {},
  },
});
numbro.setLanguage("fi-FI");


function monetary_value(input: string | number | undefined | null): string {

  if (input === undefined || input === null || input === "") {
    return "0.00€";
  }

  const value = typeof input === "number" ? input : parseFloat(input);

  return numbro(value).formatCurrency(DEFAULT_FORMAT);
}


function formula_value(input: string): React.ReactNode {

  const isRawFormat = input.toLowerCase().includes('fund units');

  try {
    const formula = _parseFormula(input);
    return (
      <>
        <Grid2 container>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight={500} variant='subtitle2'>{formula.name}</Typography>
          </Grid2>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography variant='subtitle2'>{isRawFormat ? `= ${formula.equationResult}` : `= ${monetary_value(formula.equationResult)}`}</Typography>
          </Grid2>
          <Grid2 size={{ md: 6, lg: 6, xl: 6 }}>
            <Typography variant='subtitle2' fontStyle='italic'>{formula.equationValues}</Typography>
          </Grid2>
        </Grid2>
      </>
    )
  } catch (error) {
    console.warn(`Unable to parse formula: ${input}`, error)
  }
}


interface Formula {
  name: string;
  equation: string;
  equationValues: string;
  equationResult: string;
}

function _parseFormula(input: string): Formula {
  const [name, formula] = input.split(":");
  const [equation, equationValues, equationResult] = formula.split("=");
  return {
    name,
    equation,
    equationValues,
    equationResult
  }
}

export const LedgerFormatter = {
  monetary_value,
  formula_value
}