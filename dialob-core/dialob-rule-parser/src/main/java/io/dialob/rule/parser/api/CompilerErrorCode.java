/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.rule.parser.api;

public class CompilerErrorCode {
  public static final String OPERATOR_REQUIRES_2_OPERANDS = "OPERATOR_REQUIRES_2_OPERANDS";
  public static final String ONLY_ONE_ARGUMENT_FOR_NOT = "ONLY_ONE_ARGUMENT_FOR_NOT";
  public static final String ONLY_ONE_ARGUMENT_FOR_NEGATE = "ONLY_ONE_ARGUMENT_FOR_NEGATE";
  public static final String CANNOT_EVAL_NOT_FOR_NON_BOOLEAN_TYPE = "CANNOT_EVAL_NOT_FOR_NON_BOOLEAN_TYPE";
  public static final String CANNOT_NEGATE_TYPE = "CANNOT_NEGATE_TYPE";
  public static final String CANNOT_ADD_TYPES = "CANNOT_ADD_TYPES";
  public static final String CANNOT_SUBTRACT_TYPES = "CANNOT_SUBTRACT_TYPES";
  public static final String CANNOT_MULTIPLY_TYPES = "CANNOT_MULTIPLY_TYPES";
  public static final String CANNOT_DIVIDE_TYPES = "CANNOT_DIVIDE_TYPES";
  public static final String UNKNOWN_FUNCTION = "UNKNOWN_FUNCTION";
  public static final String UNKNOWN_VARIABLE = "UNKNOWN_VARIABLE";
  public static final String COULD_NOT_DEDUCE_TYPE = "COULD_NOT_DEDUCE_TYPE";
  public static final String COMPILER_ERROR = "COMPILER_ERROR";
  public static final String BOOLEAN_VALUE_EXPECTED = "BOOLEAN_VALUE_EXPECTED";
  public static final String STRING_VALUE_EXPECTED = "STRING_VALUE_EXPECTED";
  public static final String NO_ORDER_RELATION_BETWEEN_TYPES = "NO_ORDER_RELATION_BETWEEN_TYPES";
  public static final String NO_EQUALITY_RELATION_BETWEEN_TYPES = "NO_EQUALITY_RELATION_BETWEEN_TYPES";

  public static final String REDUCER_TARGET_MUST_BE_REFERENCE = "REDUCER_TARGET_MUST_BE_REFERENCE";
  public static final String UNKNOWN_REDUCER_OPERATOR = "UNKNOWN_REDUCER_OPERATOR";
  public static final String CANNOT_USE_REDUCER_INSIDE_SCOPE = "CANNOT_USE_REDUCER_INSIDE_SCOPE";
  public static final String OPERATOR_CANNOT_REDUCE_TYPE = "OPERATOR_CANNOT_REDUCE_TYPE";

  public static final String ONLY_STRINGS_CAN_BE_MATCHED = "ONLY_STRINGS_CAN_BE_MATCHED";
  public static final String MATCHER_DEFINITION_NEEDS_TO_BE_STRING = "MATCHER_DEFINITION_NEEDS_TO_BE_STRING";
  public static final String MATCHER_REGEX_SYNTAX_ERROR = "MATCHER_REGEX_SYNTAX_ERROR";
  public static final String MATCHER_DYNAMIC_REGEX = "MATCHER_DYNAMIC_REGEX";
  public static final String SYNTAX_ERROR = "SYNTAX_ERROR";
  public static final String AMBIGUOUS_INPUT = "AMBIGUOUS_INPUT";
  public static final String CONTEXT_SENSITIVE_ERROR = "CONTEXT_SENSITIVE_ERROR";
  public static final String FULL_CONTEXT_ERROR = "FULL_CONTEXT_ERROR";

  public static final String INCOMPLETE_EXPRESSION = "INCOMPLETE_EXPRESSION";

  public static final String ARRAY_TYPE_EXPECTED = "ARRAY_TYPE_EXPECTED";
  public static final String ARRAY_TYPE_UNEXPECTED = "ARRAY_TYPE_UNEXPECTED";

}
