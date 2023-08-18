import { useContext } from "react"
import { ComposerContext } from './ComposerContext';
import { DialobItem, DialobItemTemplate, ValueSetEntry, ContextVariableType, ValidationRule } from "../types";

export const useComposer = () => {
  const { state, dispatch } = useContext(ComposerContext);

  const addItem = (itemTemplate: DialobItem, parentItemId: string, afterItemId ?: string): void => {
    dispatch({type: 'addItem', config: itemTemplate, parentItemId, afterItemId});
  };

  const updateItem = (itemId: string, attribute: string, value: string, language?: string) => {
    dispatch({type: 'updateItem', itemId, attribute, value, language});
  };

  const changeItemType = (itemId: string, config: DialobItemTemplate) => {
    dispatch({type: 'changeItemType', itemId, config});
  };

  const deleteItem = (itemId: string) => {
    dispatch({type: 'deleteItem', itemId});
  };

  const setItemProp = (itemId: string, key: string, value: any) => {
    dispatch({type: 'setItemProp', itemId, key, value});
  }

  const deleteItemProp = (itemId: string, key: string) => {
    dispatch({type: 'deleteItemProp', itemId, key});
  }

  const moveItem = (itemId: string, fromIndex: number, toIndex: number, fromParent: string, toParent: string) => {
    dispatch({type: 'moveItem', itemId, fromIndex, toIndex, fromParent, toParent});
  }

  const createValidation = (itemId: string, rule?: ValidationRule) => {
    dispatch({type: 'createValidation', itemId, rule});
  }

  const setValidationMessage = (itemId: string, index: number, language: string, message: string) => {
    dispatch({type: 'setValidationMessage', itemId, index, language, message});
  }

  const setValidationExpression = (itemId: string, index: number, expression: string) => {
    dispatch({type: 'setValidationExpression', itemId, index, expression});
  }

  const deleteValidation = (itemId: string, index: number) => {
    dispatch({type: 'deleteValidation', itemId, index});
  }

  const createValueSet = (itemId: string | null, entries?: ValueSetEntry[]) => {
    dispatch({type: 'createValueSet', itemId, entries });
  };

  const setValueSetEntries = (valueSetId: string, entries: ValueSetEntry[]) => {
    dispatch({type: 'setValueSetEntries', valueSetId, entries});
  }

  const addValueSetEntry = (valueSetId: string, entry?: ValueSetEntry) => {
    dispatch({type: 'addValueSetEntry', valueSetId, entry});
  }

  const updateValueSetEntry = (valueSetId: string, index: number, entry: ValueSetEntry) => {
    dispatch({type: 'updateValueSetEntry', valueSetId, index, entry});
  }

  const deleteValueSetEntry = (valueSetId: string, index: number) => {
    dispatch({type: 'deleteValueSetentry', valueSetId, index});
  }

  const moveValueSetEntry = (valueSetId: string, from: number, to: number) => {
    dispatch({type: 'moveValueSetEntry', valueSetId, from, to});
  }

  const setGlobalValueSetName = (valueSetId: string, name: string) => {
    dispatch({type: 'setGlobalValueSetName', valueSetId, name});
  }

  const setMetadataValue = (attr: string, value: any) => {
    dispatch({type: 'setMetadataValue', attr, value});
  }

  const createVariable = (context: boolean) => {
    dispatch({type: 'createVariable', context});
  }

  const updateContextVariable = (variableId: string, contextType: ContextVariableType, defaultValue?: any) => {
    dispatch({type: 'updateContextVariable', variableId, defaultValue, contextType});
  }

  const updateExpressionVariable = (variableId: string, expression: string) => {
    dispatch({type: 'updateExpressionVariable', variableId, expression});
  }

  const deleteVariable = (variableId: string) => {
    dispatch({type: 'deleteVariable', variableId});
  }

  const addLanguage = (language: string, copyFrom?: string) => {
    dispatch({type: 'addLanguage', language, copyFrom});
  }

  const deleteLanguage = (language: string) => {
    dispatch({type: 'deleteLanguage', language});
  }

  return {
    addItem,
    updateItem,
    changeItemType,
    deleteItem,
    setItemProp,
    deleteItemProp,
    moveItem,
    createValidation,
    setValidationMessage,
    setValidationExpression,
    deleteValidation,
    createValueSet,
    setValueSetEntries,
    addValueSetEntry,
    updateValueSetEntry,
    deleteValueSetEntry,
    moveValueSetEntry,
    setGlobalValueSetName,
    setMetadataValue,
    createVariable,
    updateContextVariable,
    updateExpressionVariable,
    deleteVariable,
    addLanguage,
    deleteLanguage
  };

}