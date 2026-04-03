# limaone (L1) Model Documentation Context

## Overview
limaone (L1) is a comprehensive digital government platform that combines content management, forms (Dialob), workflows (theWrench), and content publishing (theStencil). This document provides the baseline context for understanding the core model entities, their relationships, and transformation patterns within the limaone ecosystem.

## Platform Components
- **Dialob**: Open source low-code form development tool with Dialob Composer
- **theWrench**: Open source low-code microservices development tool with theWrench Composer  
- **theStencil**: Content management tool for DigiExpress service portal
- **limaone**: The underlying model and authoring framework

## Source Analysis Summary

### Original Stencil Entities
Located in `io.thestencil.client.api`:
- **StencilClient.java**: Core entity definitions (Article, Page, Link, Workflow, Template, Locale, LocaleLabel)
- **CreateBuilder.java**: Creation POJOs (CreateArticle, CreatePage, etc.) 
- **UpdateBuilder.java**: Update POJOs (ArticleMutator, PageMutator, etc.)

### Migrated limaone Model Entities
Located in `io.resys.limaone.model`:
- **Model.java**: Generic wrapper with BodyType enum classification
- **Article.java**: Content articles with hierarchy
- **ArticlePage.java**: Localized content pages
- **ArticleLink.java**: Navigation and workflow links
- **ArticleWorkflow.java**: Business process workflows  
- **ArticleTemplate.java**: Content templates
- **Locale.java**: Language/region definitions
- **LocaleLabel.java**: Localized label translations

### Authoring API Patterns
Located in `io.resys.limaone.authoring`:
- **New*** interfaces**: Creation builders with dual props() methods
- **Modify*** interfaces**: Update builders with dual props() methods
- **Package structure**: Organized by entity type (article/, locale/, articlepage/, etc.)

## Model Entity Catalog

### Core Model Infrastructure
```java
Model<T extends Body> {
  String getId();
  T getBody(); 
  BodyType getType();
}

Model.Body extends Serializable {
  // Base interface for all entities
}

Model.BodyType enum {
  LOCALE,
  ARTICLE_ARTICLE, ARTICLE_PAGE, ARTICLE_LINK, ARTICLE_WORKFLOW, ARTICLE_TEMPLATE,
  FLOW, FLOW_TASK, DECISION_TABLE
}
```

### Article-based Entities

#### Article
**Purpose**: Core content articles with hierarchical structure  
**Fields**:
- Required: `String getName()`, `Integer getOrder()`
- Optional: `String getParentId()`, `Boolean getDevMode()`, `Boolean getAuthOnly()`

#### ArticlePage  
**Purpose**: Localized content pages linked to articles
**Fields**:
- Required: `String getArticle()`, `String getLocale()`, `String getContent()`
- Optional: `Boolean getDevMode()`

#### ArticleLink
**Purpose**: Navigation links and external references
**Fields**:
- Required: `String getValue()`, `String getContentType()`, `List<String> getArticles()`, `List<LocaleLabel> getLabels()`
- Optional: `Boolean getDevMode()`

#### ArticleWorkflow
**Purpose**: Business process workflows with form integration
**Fields**:
- Required: `String getValue()`, `List<String> getArticles()`, `List<LocaleLabel> getLabels()`
- Optional: Form fields (`String getFormId()`, `getFormName()`, `getFormTag()`, `getFlowName()`)
- Optional: Behavior flags (`Boolean getDevMode()`, `getAssignable()`, `getAnon()`, `getDisabled()`)
- Optional: Dates (`OffsetDateTime getStartDate()`, `getEndDate()`)

#### ArticleTemplate
**Purpose**: Content templates for reusable structure
**Fields**:
- Required: `String getName()`, `String getDescription()`, `String getContent()`, `String getType()`

### Standalone Entities

#### Locale
**Purpose**: Language and region definitions
**Fields**:
- Required: `String getValue()`, `Boolean getEnabled()`

#### LocaleLabel  
**Purpose**: Localized translations for UI elements
**Fields**:
- Required: `String getLocale()`, `String getLabelValue()`

### Flow Entities (Defined but not implemented)
- **Flow**: Main workflow definitions
- **FlowTask**: Individual workflow tasks
- **DecisionTable**: Decision logic tables

## Entity Relationships

### Hierarchical Relationships
- **Article → Article**: Parent-child hierarchy via `getParentId()`
- **Article → ArticlePage**: One-to-many via `getArticle()`

### Reference Relationships  
- **ArticleLink → Article**: Many-to-many via `getArticles()`
- **ArticleWorkflow → Article**: Many-to-many via `getArticles()`
- **ArticleLink → LocaleLabel**: One-to-many via `getLabels()`
- **ArticleWorkflow → LocaleLabel**: One-to-many via `getLabels()`

### Form Integration
- **ArticleWorkflow → Dialob Forms**: Via `getFormId()`, `getFormName()`, `getFormTag()`
- **ArticleWorkflow → theWrench Flows**: Via `getFlowName()`

## Transformation Patterns

### Stencil → limaone Migration
1. **Entity Extraction**: Inner interfaces → Standalone classes
2. **Naming Convention**: Add "Article" prefix for content entities
3. **Inheritance Change**: `EntityBody` → `Model.Body`  
4. **Field Reordering**: Required fields first, nullable fields grouped
5. **Return Types**: `Uni<Entity<T>>` → `Uni<Model<T>>`

### Field Mapping Examples
- `StencilClient.Link` → `ArticleLink`
- `StencilClient.Page` → `ArticlePage`  
- `CreateArticle` → `NewArticleProps`
- `ArticleMutator` → `ModifyArticleProps`

## API Patterns

### CRUD Operations
```
POST /articles     → NewArticle.build()
PUT /articles/{id} → ModifyArticle.build() 
DELETE /articles/{id} → DeleteSource (TBD)
GET /site          → Compiled site structure
```

### Authoring Interface Pattern
```java
interface NewEntity {
  NewEntity props(NewEntityProps props);
  NewEntity props(Consumer<ImmutableNewEntityProps.Builder> props);
  Uni<Model<EntityType>> build();
}

interface ModifyEntity {
  ModifyEntity props(ModifyEntityProps props);  
  ModifyEntity props(Consumer<ImmutableModifyEntityProps.Builder> props);
  Uni<Model<EntityType>> build();
}
```

### Package Organization
- `article/`: Article entity and operations
- `locale/`: Locale entity and operations
- `articlepage/`: ArticlePage entity and operations
- `articlelink/`: ArticleLink entity and operations
- `articleworkflow/`: ArticleWorkflow entity and operations
- `articletemplate/`: ArticleTemplate entity and operations

## Runtime Structure

### Compiled Site JSON
When published, limaone entities compile into a customer-facing site structure:

```json
{
  "id": "site-id",
  "locale": "en",
  "topics": {
    "article-path": {
      "id": "article-id", 
      "name": "Article Name",
      "links": ["link-ids"],
      "headings": [{"id", "name", "order", "level"}],
      "parent": "parent-path",
      "blob": "content-blob-id"
    }
  },
  "blobs": {
    "blob-id": {
      "id": "blob-id",
      "value": "markdown content"
    }
  },
  "links": {
    "link-id": {
      "id": "link-id",
      "path": "article-path", 
      "type": "external|phone|workflow",
      "name": "Link Name",
      "value": "URL or form reference",
      "workflow": true/false,
      "formId": "dialob-form-id",
      "flowName": "wrench-flow-name"
    }
  },
  "workflowsInOtherLocales": {
    "fi": [...],
    "sv": [...], 
    "en": [...]
  }
}
```

### Entity → JSON Mapping
- **Article** → `topics` with hierarchical paths
- **ArticlePage** → `blobs` with markdown content
- **ArticleLink** → `links` with type-specific behaviors
- **ArticleWorkflow** → `links` with `workflow: true` and form references
- **Locale** → Site-level locale configuration

## Integration Points

### Form System (Dialob)
- ArticleWorkflow references forms via `formId`, `formName`, `formTag`
- Forms handle user input collection
- Integration enables dynamic service creation

### Workflow System (theWrench)  
- ArticleWorkflow references flows via `flowName`
- Flows handle business logic and automation
- Integration enables complex service orchestration

### Content Management (theStencil)
- Articles provide site structure and navigation
- ArticlePages provide localized content
- ArticleLinks provide navigation and service access
- ArticleTemplates provide content reusability

## Development Patterns

### Creation Flow
1. Define entity via New* authoring interface
2. Set properties using props() methods
3. Build entity via build() → creates Model<T>
4. Persist and compile for site publication

### Modification Flow  
1. Load existing entity
2. Define changes via Modify* authoring interface
3. Set updated properties using props() methods
4. Build updated entity via build() → creates Model<T>
5. Persist and recompile for site publication

### Multi-language Support
- Locale entities define supported languages
- ArticlePage provides content per locale
- LocaleLabel provides UI translations per locale
- Site compilation includes `workflowsInOtherLocales` structure

## Notes for Future Documentation
- Flow entities (Flow, FlowTask, DecisionTable) are defined but not implemented
- DeleteSource operations are defined but not implemented  
- PDF generation and printing capabilities exist but are not modeled here
- This represents the content authoring layer, not the complete platform