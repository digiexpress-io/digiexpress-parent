import React from 'react';

interface ValueSetEntry {
  id: string;
  label: any;
  [x: string]: any;
}

interface ValueSet {
  id: string;
  entries: ValueSetEntry[];
  [x: string]: any;
}

export interface SurveyGroupContextType {
  surveyValueSet: ValueSet | null
}

const context = React.createContext<SurveyGroupContextType>({surveyValueSet:null});
export const SurveyGroupContext = context;
