import React from 'react';


function monetary_value(input: string | number | undefined | null): string {
  if(input === undefined) {
    return "--";
  }

  return "";
}


function formula_value(input: string): React.ReactNode {

  const formula = _parseFormula(input);

  // Payment Fee    : κ × gross_amount  =   0 × 1525.00 = 0.00
  // Net Payment    : gross_amount - payment_fee = 1525.00 - 0.00 = 1525.00
  // Allocation     : net_payment × allocation_share = 1525.00 × 0.700000 = 1067.50
  // Fund Units     : allocated_amount ÷ unit_price = 1067.50 ÷ 100.54000000 = 10.617665

  return (<></>)


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