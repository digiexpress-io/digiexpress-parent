package io.resys.limaone.spi.program.article;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.limaone.ast.Article_AST;
import io.resys.limaone.ast.Article_AST.ImageTag;
import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import jakarta.annotation.Nullable;

public interface SiteVisitor {
  void visitTopicData(TopicData topic);
  void visitLinkData(LinkData link);
  void visitImageData(ImageData image);
  void visitTopicNameData(TopicNameData names);
  SiteVisitorOutput visit(String imageUrl);
  
  @Value.Immutable
  interface SiteVisitorOutput {
    List<Message> getMessage();
    List<LocalizedSite> getSites();
  }
  
  @Value.Immutable
  interface Message {
    String getText();
    @Nullable
    Object getObject();
  }

  @Value.Immutable
  interface TopicData {
    String getPath();
    String getLocale();
    String getValue();
    Boolean getAuth();
    List<Article_AST.Heading> getHeadings();
    List<ImageTag> getImages();
  }

  @Value.Immutable
  interface TopicNameData {
    String getPath();
    Map<String, String> getLocale();
  }

  @Value.Immutable
  interface LinkData {
    String getId();
    String getPath();
    String getName();
    String getType();
    String getValue();
    String getLocale();
    Boolean getGlobal();
    Boolean getAnon();
    Boolean getAssignable();
    Boolean getWorkflow();
    @Nullable LocalDateTime getStartDate();
    @Nullable LocalDateTime getEndDate();
    @Nullable String getFormId();
    @Nullable String getFormName();
    @Nullable String getFormTag();
    @Nullable String getFlowName();
  }

  @Value.Immutable
  interface ImageData {
    String getPath();
    byte[] getValue();
  }
}
