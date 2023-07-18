package io.resys.hdes.ast.spi;

import java.util.Collections;
import java.util.List;

/*-
 * #%L
 * hdes-ast
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.util.Optional;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.spi.antlr.visitors.returntype.flowstep.FlowStepEndFinder;
import io.resys.hdes.ast.spi.antlr.visitors.returntype.flowstep.FlowStepFinder;
import io.resys.hdes.ast.spi.antlr.visitors.returntype.flowstep.FlowWakeUpFinder;
import io.resys.hdes.ast.spi.returntypes.ReturnTypeDtVisitor;
import io.resys.hdes.ast.spi.returntypes.ReturnTypeExpVisitor;
import io.resys.hdes.ast.spi.returntypes.ReturnTypeFlStepAsDefVisitor;
import io.resys.hdes.ast.spi.returntypes.ReturnTypeFlStepDefVisitor;
import io.resys.hdes.ast.spi.returntypes.ReturnTypeFlVisitor;
import io.resys.hdes.ast.spi.returntypes.ReturnTypeStVisitor;
import io.resys.hdes.ast.spi.util.Assertions;

public class ImmutableHdesTree implements HdesTree {

  private final Optional<HdesTree> parent;
  private final HdesNode value;
  
  private final ReturnTypeExpVisitor expressionReturnsDefVisitor = new ReturnTypeExpVisitor();
  private final ReturnTypeDtVisitor decisionTableInvocationReturnTypeVisitor = new ReturnTypeDtVisitor();
  private final ReturnTypeFlVisitor flowInvocationReturnTypeVisitor = new ReturnTypeFlVisitor();
  private final ReturnTypeStVisitor serviceInvocationReturnTypeVisitor = new ReturnTypeStVisitor();
  private final ReturnTypeFlStepAsDefVisitor flStepAsDefVisitor = new ReturnTypeFlStepAsDefVisitor();
  
  private ImmutableHdesTree(Optional<HdesTree> parent, HdesNode value) {
    super();
    this.parent = parent;
    this.value = value;
  }
  
  @SuppressWarnings("unchecked")
  private <T extends BodyNode> Optional<T> getBody() {
    HdesTree parent = this;
    do {
      if(parent.getValue() instanceof BodyNode) {
        return Optional.of((T) parent.getValue());
      } else {
        parent = parent.getParent().orElse(null);
      }
    } while(parent != null);
    
    return Optional.empty();
  }
  
  @Override
  public RootNode getRoot() {
    return get().node(RootNode.class);
  }
  
  @Override
  public Optional<HdesTree> getParent() {
    return parent;
  }

  @Override
  public HdesNode getValue() {
    return value;
  }

  @Override
  public HdesTree next(HdesNode next) {
    return ImmutableHdesTree.builder().parent(this).value(next).build();
  }
  
  public NodeFindQuery find() {
    HdesTree that = this;
    return new NodeFindQuery() {
      private Optional<Class<?>> limit = Optional.empty();
      @SuppressWarnings("unchecked")
      @Override
      public <T extends HdesNode> Optional<T> node(Class<T> type) {
        HdesTree iterator = that;
        do {
          if(limit.isPresent() && limit.get().isAssignableFrom(iterator.getValue().getClass())) {
            return Optional.empty();
          }
          
          if(type.isAssignableFrom(iterator.getValue().getClass())) {
            return Optional.of((T) iterator.getValue());
          } else {
            iterator = iterator.getParent().orElse(null);
          }

        } while(iterator != null);
        
        return Optional.empty();
      }
      
      @Override
      public <T extends HdesNode> Optional<HdesTree> ctx(Class<T> type) {
        HdesTree parent = that;
        do {
          if(type.isAssignableFrom(parent.getValue().getClass())) {
            return Optional.of(parent);
          } else {
            parent = parent.getParent().orElse(null);
          }
        } while(parent != null);
        
        return Optional.empty();
      }

      @Override
      public <T extends HdesNode> NodeFindQuery limit(Class<T> limit) {
        this.limit = Optional.ofNullable(limit);
        return this;
      }
    };
  }

  @Override
  public NodeGetQuery get() {
    return new NodeGetQuery() {
      @Override
      public <T extends HdesNode> T node(Class<T> type) {
        
        List<String> x;
        Optional<T> node = find().node(type);
        if(node.isEmpty()) {
          throw new HdesException("Context does not contain node: " + type + "!");
        }
        return node.get();
      }
      @Override
      public <T extends HdesNode> HdesTree ctx(Class<T> type) {
        Optional<HdesTree> ctx = find().ctx(type);
        if(ctx.isEmpty()) {
          throw new HdesException("Context does not contain node: " + type + "!");
        }
        return ctx.get();
      }
      @Override
      public String bodyId() {
        return body().getId().getValue();
      }
      @Override
      public BodyNode body() {
        Optional<BodyNode> body = getBody();
        if(body.isEmpty()) {
          throw new HdesException("Context does not contain body node!");
        }
        return body.get();
      }
    };
  }
  
  @Override
  public TypeDefAnyQuery any() {
    BodyNode body = null;
    
    HdesTree iterable = this;
    while(iterable != null) {
      iterable = iterable.get().ctx(BodyNode.class);
      body = (BodyNode) iterable.getValue();
      if(!(body instanceof ExpressionBody)) {
        break;
      }
      iterable = iterable.getParent().get();
    }
    
    HdesTree ctx = this;
    
    if(body instanceof DecisionTableBody) {
      return new TypeDefAnyQuery() {
        @Override
        public TypeDef build(InvocationNode name) {
          return decisionTableInvocationReturnTypeVisitor.visitBody(name, ctx);
        }
      };
    }
    if(body instanceof FlowBody) {
      return new TypeDefAnyQuery() {
        @Override
        public TypeDef build(InvocationNode name) {
          return flowInvocationReturnTypeVisitor.visitBody(name, ctx);
        }
      };
    }
    if(body instanceof ServiceBody) {
      return new TypeDefAnyQuery() {
        @Override
        public TypeDef build(InvocationNode name) {
          return serviceInvocationReturnTypeVisitor.visitBody(name, ctx);
        }
      };
    }
    
    throw new HdesException("Unknown body node: " + body.getClass() + "!");
  }

  @Override
  public TypeDefReturnsQuery returns() {
    HdesTree ctx = this;
    return new TypeDefReturnsQuery() {
      @Override
      public TypeDefReturns build(HdesNode src) {
        Assertions.notNull(src, () -> "src can't be null!");
        return expressionReturnsDefVisitor.visit(src, ctx);
      }
    };
  }
  
  @Override
  public StepQuery step() {
    HdesTree ctx = this;
    return new StepQuery() {
      @Override
      public Optional<Step> findStep(String id, Optional<Step> children) {
        if(children.isEmpty()) {
          return Optional.empty();
        }
        return new FlowStepFinder(id).visitBody(children.get(), ImmutableHdesTree.builder().value(children.get()).build());
      }
      
      @Override
      public ObjectDef getDef(Step target) {
        return (ObjectDef) new ReturnTypeFlStepDefVisitor().visitBody(target, ctx);
      }
      
      @Override
      public Optional<ObjectDef> findEnd(Optional<Step> step) {
        if(step.isEmpty()) {
          return Optional.empty();
        }
        List<TypeDef> defs = new FlowStepEndFinder().visitBody(step.get(), ctx).getValues();
        if(defs.size() == 1) {
          return Optional.of((ObjectDef) defs.get(0));
        } else if(defs.size() > 1) {
          return Optional.of((ObjectDef) FlowStepEndFinder.merge(defs, ctx));
        }
        return Optional.empty();
      }

      @Override
      public List<InvocationNode> getWakeUps(Optional<Step> start) {
        if(start.isEmpty()) {
          return Collections.emptyList();
        }
        return new FlowWakeUpFinder().visitBody(start.get(), ctx).getValues();
      }

      @Override
      public Optional<ObjectDef> getDefAs(Step target) {
        return flStepAsDefVisitor.visitBody(target, ctx);
      }
    };
  }
  
  public static class ImmutableRootVisitorTree extends ImmutableHdesTree implements RootTree {
    public ImmutableRootVisitorTree(Optional<HdesTree> parent, RootNode value) {
      super(parent, value);
    }
    @Override
    public RootNode getValue() {
      return (RootNode) super.getValue();
    }
  }
  
  public static class ImmutableDtVisitorTree extends ImmutableHdesTree implements DecisionTableTree {
    public ImmutableDtVisitorTree(Optional<HdesTree> parent, DecisionTableBody value) {
      super(parent, value);
    }
    @Override
    public DecisionTableBody getValue() {
      return (DecisionTableBody) super.getValue();
    }
    @Override
    public RootNode getRoot() {
      return (RootNode) getParent().get().getValue();
    }
  }
  
  public static class ImmutableFlVisitorTree extends ImmutableHdesTree implements FlowTree {
    public ImmutableFlVisitorTree(Optional<HdesTree> parent, FlowBody value) {
      super(parent, value);
    }
    @Override
    public FlowBody getValue() {
      return (FlowBody) super.getValue();
    }
    @Override
    public RootNode getRoot() {
      return (RootNode) getParent().get().getValue();
    }
  }
  
  public static class ImmutableStVisitorTree extends ImmutableHdesTree implements ServiceTree {
    public ImmutableStVisitorTree(Optional<HdesTree> parent, ServiceBody value) {
      super(parent, value);
    }
    @Override
    public ServiceBody getValue() {
      return (ServiceBody) super.getValue();
    }
    @Override
    public RootNode getRoot() {
      return (RootNode) getParent().get().getValue();
    }
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private HdesTree parent;
    private HdesNode value;
    
    public Builder parent(HdesTree parent) {
      this.parent = parent;
      return this;
    }
    
    public Builder value(HdesNode value) {
      this.value = value;
      return this;
    }

    public ImmutableDtVisitorTree dt(DecisionTableBody value) {
      Assertions.notNull(parent, () -> "parent can't be null!");
      Assertions.isTrue(parent.find().node(RootNode.class).isPresent(), () -> "parent must have RootNode as value!");
      return (ImmutableDtVisitorTree) value(value).build();
    }
    
    public ImmutableFlVisitorTree fl(FlowBody value) {
      Assertions.notNull(parent, () -> "parent can't be null!");
      Assertions.isTrue(parent.find().node(RootNode.class).isPresent(), () -> "parent must have RootNode as value!");
      return (ImmutableFlVisitorTree) value(value).build();
    }
    
    public ImmutableStVisitorTree st(ServiceBody value) {
      Assertions.notNull(parent, () -> "parent can't be null!");
      Assertions.isTrue(parent.find().node(RootNode.class).isPresent(), () -> "parent must have RootNode as value!");
      return (ImmutableStVisitorTree) value(value).build();
    }
    
    public ImmutableRootVisitorTree root(RootNode value) {
      Assertions.notNull(value, () -> "value can't be null!");
      return (ImmutableRootVisitorTree) value(value).build();
    }
    
    public ImmutableHdesTree build() {
      Assertions.notNull(value, () -> "value can't be null!");
      
      final Optional<HdesTree> parent = Optional.ofNullable(this.parent);
      
      if(value instanceof DecisionTableBody) {
        return new ImmutableDtVisitorTree(parent, (DecisionTableBody) value);
      } else if(value instanceof ServiceBody) {
        return new ImmutableStVisitorTree(parent, (ServiceBody) value);
      } else if(value instanceof FlowBody) {
        return new ImmutableFlVisitorTree(parent, (FlowBody) value);
      } else if(value instanceof RootNode) {
        return new ImmutableRootVisitorTree(parent, (RootNode) value);
      }

      return new ImmutableHdesTree(parent, value);
    }
  }
}
