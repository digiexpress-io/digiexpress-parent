import { DialobItemTemplate, ValueSetEntry, ContextVariableType, ValidationRule } from "./types";

export type ComposerAction =
  | { type: 'addItem', config: DialobItemTemplate, parentItemId: string, afterItemId?: string}
  | { type: 'updateItem', itemId: string, attribute: string, value: any, language?: string}
  | { type: 'changeItemType', itemId: string, config: DialobItemTemplate}
  | { type: 'deleteItem', itemId: string}
  | { type: 'setItemProp', itemId: string, key: string, value: any}
  | { type: 'deleteItemProp', itemId: string, key: string}
  | { type: 'moveItem', itemId: string, fromIndex: number, toIndex: number, fromParent: string, toParent: string}

  | { type: 'createValidation', itemId: string, rule?: ValidationRule}
  | { type: 'setValidationMessage', itemId: string, index: number, language: string, message: string}
  | { type: 'setValidationExpression', itemId: string, index: number, expression: string}
  | { type: 'deleteValidation', itemId: string, index: number}

  | { type: 'createValueSet', itemId: string | null, entries?: ValueSetEntry[]}
  | { type: 'setValueSetEntries', valueSetId: string, entries: ValueSetEntry[]}
  | { type: 'addValueSetEntry', valueSetId: string, entry?: ValueSetEntry}
  | { type: 'updateValueSetEntry', valueSetId: string, index: number, entry: ValueSetEntry}
  | { type: 'deleteValueSetentry', valueSetId: string, index: number}
  | { type: 'moveValueSetEntry', valueSetId: string, from: number, to: number}
  | { type: 'setGlobalValueSetName', valueSetId: string, name: string}
  // | { type: 'updateValueSetEntryAttr', valueSetId: string, index: number, attr: string, value: any } // probably obsoleted by updateValueSetEntry

  | { type: 'setMetadataValue', attr: string, value: any}

  | { type: 'createVariable', context: boolean}
  | { type: 'updateContextVariable', variableId: string, defaultValue?: any, contextType ?: ContextVariableType }
  | { type: 'updateExpressionVariable', variableId: string, expression: string} 
  | { type: 'deleteVariable', variableId: string}

  | { type: 'addLanguage', language: string, copyFrom?: string}
  | { type: 'deleteLanguage', language: string}

