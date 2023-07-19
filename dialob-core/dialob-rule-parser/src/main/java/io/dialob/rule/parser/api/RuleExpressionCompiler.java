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

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public interface RuleExpressionCompiler {

    /**
     *
     *
     * @param expression
     * @param compilationResultListener
     * @return true is expression compilation was ok.
     * @deprecated @{see io.dialob.session.engine.program.ddrl.DDRLExpressionCompiler#compile}
     */
    @Deprecated
    boolean compile(String expression, VariableFinder variableFinder, RuleExpressionCompilerCallback compilationResultListener);

    @NotNull
    UnaryOperator<String> createIdRenamer(String oldId, String newId);

}
