package io.resys.limaone.spi.compiler.article;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.limaone.ast.Article_AST;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ArticleProgram;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.Runtime;


public class ImmutableArticleProgram implements ArticleProgram {

  private static final long serialVersionUID = -5191441381711565812L;
  private final List<OffsetDateTime> refreshDates;
  
  private final Article_AST articleAST;
  private final ProgramStatus status;
  private final List<ProgramMessage> errors;
  private final List<ProgramAssociation> assocs;
  private final List<LocalizedSite> sites;
  
  public ImmutableArticleProgram(
      Article_AST articleAST, 
      ProgramStatus status,
      List<ProgramMessage> errors, 
      List<ProgramAssociation> assocs,
      List<LocalizedSite> sites) {
    
    super();
    this.articleAST = articleAST;
    this.status = status;
    this.errors = errors;
    this.assocs = assocs;
    this.sites = sites;
    
    final var refreshDates = new ArrayList<OffsetDateTime>();
    for(final var site : sites) {
      for(final var date : site.getRefreshDates()) {
        if(!refreshDates.contains(date)) {
          refreshDates.add(date);
        }
      }
    }
    this.refreshDates = Collections.unmodifiableList(refreshDates);
  }
  
  @Override
  public String getId() {
    return articleAST.getId();
  }
  @Override
  public String getName() {
    return articleAST.getName();
  }
  @Override
  public BodyType getType() {
    return BodyType.ARTICLE;
  }
  @Override
  public ProgramStatus getStatus() {
    return status;
  }
  @Override
  public List<Parameter> getHeaders() {
    return Collections.emptyList();
  }
  @Override
  public List<ProgramMessage> getErrors() {
    return errors;
  }
  @Override
  public List<ProgramAssociation> getAssociations() {
    return assocs;
  }
  @Override
  public List<OffsetDateTime> getRefreshDates() {
    return refreshDates;
  }
  @Override
  public ArticleProgramResult run(ProgramInput input, Runtime context) {

    // ArticleProgram

    
    // 1. target date when running, 2. AUTH when running
    /**
     *         boolean requiredAuth = Boolean.TRUE.equals(topic.getAuth());
        boolean isUserAuthenticated = this.auth;
        if(requiredAuth) {
          return isUserAuthenticated;  
        }
     */
    //todo
    return null;
  }
}
