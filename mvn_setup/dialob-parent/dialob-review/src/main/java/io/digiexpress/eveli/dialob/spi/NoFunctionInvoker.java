package io.digiexpress.eveli.dialob.spi;

import io.dialob.session.engine.program.EvalContext.UpdatedItemsVisitor.AsyncFunctionCallVisitor;
import io.dialob.session.engine.session.AsyncFunctionCall;
import io.dialob.session.engine.sp.AsyncFunctionInvoker;

public class NoFunctionInvoker extends AsyncFunctionInvoker {

  public NoFunctionInvoker() {
    super(null, null);
  }

  @Override
  public AsyncFunctionCallVisitor createVisitor(String sessionId) {
    return new AsyncFunctionCallVisitor() {
      @Override
      public void visitAsyncFunctionCall(AsyncFunctionCall asyncFunctionCall) {
      }
    };
  }

}
