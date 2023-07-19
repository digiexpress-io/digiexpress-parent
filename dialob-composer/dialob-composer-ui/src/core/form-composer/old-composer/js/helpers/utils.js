import memoize from 'memoizee';

export function findRoot(data) {
  if (!data) {return null;}
  return data.find((v, k) => v.get('type') === 'questionnaire');
}

export function findValueset(data, id) {
  if (!data || !data.get('valueSets') || !id) {return null;}
  return data.get('valueSets').find(v => v.get('id') === id);
}

export function isGlobalValueSet(globalValueSets, id) {
  const gvsIndex = globalValueSets ? globalValueSets.findIndex(vs => vs.get('valueSetId') === id) : -1;
  return gvsIndex > -1;
}

export function translateErrorType(error) {
  switch (error.get('type')) {
    case 'VARIABLE':
      return 'Variable';
    case 'VISIBILITY':
      return 'Visibility';
    case 'GENERAL':
      return error.get('message') === 'INVALID_DEFAULT_VALUE' ? 'Default' : '';
    case 'REQUIREMENT':
      return 'Requirement';
    case 'VALIDATION':
      return 'Validation';
    case 'VALUESET':
      return 'List';
    case 'VALUESET_ENTRY':
      return 'Liet entry';
    default:
      return error.get('type');
  };
}

export function translateErrorMessage(error) {
  switch (error.get('message')) {
    case 'RB_VARIABLE_NEEDS_EXPRESSION':
      return 'Missing expression';
    case 'INVALID_DEFAULT_VALUE':
      return 'Invalid value';
    case 'UNKNOWN_VARIABLE':
      return 'Unknown variable';
    case 'SYNTAX_ERROR':
      return 'Syntax error';
    case 'COULD_NOT_DEDUCE_TYPE':
      return 'Can\'t deduce type ';
    case 'NO_ORDER_RELATION_BETWEEN_TYPES':
      return 'Can\'t compare these variables';
    case 'NO_EQUALITY_RELATION_BETWEEN_TYPES':
      return 'Can\'t compare these variables';
    case 'BOOLEAN_EXPRESSION_EXPECTED':
      return 'Boolean expression expected';
    case 'VALUESET_EMPTY':
      return 'Choice list is empty';
    case 'VALUESET_DUPLICATE_KEY':
      return `Choice list has duplicate key '${error.get('expression')}'`;
    case 'VALUESET_EMPTY_KEY':
      return 'Choice list has empty key';
    case 'CONTEXT_VARIABLE_UNDEFINED_TYPE':
      return 'Context variable type not defined';
    case 'VALUE_TYPE_NOT_SET':
      return 'Value type not set';
    case 'TAG_EXISTS':
      return 'Tag already exists';
    case 'MATCHER_REGEX_SYNTAX_ERROR':
      return 'Invalid regular expression';
    case 'MATCHER_DYNAMIC_REGEX':
      return 'Dynamic regular expressions not supported';
    case 'REDUCER_TARGET_MUST_BE_REFERENCE':
      return 'Multirow aggregate function target must be directly multirow item';
    case 'CANNOT_USE_REDUCER_INSIDE_SCOPE':
      return 'Multirow aggregate function can\'t be used for non-multirow item';
    case 'UNKNOWN_REDUCER_OPERATOR':
      return 'Unknown multirow aggregate function';
    case 'OPERATOR_CANNOT_REDUCE_TYPE':
      return 'This aggregate function can\'t be used for this item type';
    case 'UNKNOWN_FUNCTION':
      return 'Undefined function';

    default:
      return error.get('message');
  };
}

const findItemTypeConfig = memoize((itemTypes, viewType) => {
    if (!viewType) {
      return null;
    }
    for (let idx in itemTypes.categories) {
      const c = itemTypes.categories[idx];
      const resultConfig = c.items.find(v => v.config.view === viewType);
      if (resultConfig) {
        return resultConfig;
      }
    }
    for (let idx in itemTypes.categories) {
      const c = itemTypes.categories[idx];
      const resultConfig = c.items.find(v => v.config.type === viewType);
      if (resultConfig) {
        return resultConfig;
      }
    }
    return null;
  }
);

export {findItemTypeConfig};
