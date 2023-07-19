=== SITE ===
GET /site
{
'articles': ... ,
'pages': ... ,
'links' ...
...
} 

=== ARTICLE ===
POST /articles
DELETE /articles/{articleId}
PUT /articles/{articleId}

1. Create a new article
2. Delete an article by id
3. Change an article name, parent, order



=== PAGE ===
POST /pages
DELETE /pages/{pageId}
PUT /pages/{pageId}   


1. Create a new page
2. Delete a page
3. Change a page content, locale, article



=== LOCALE ===
POST /locales
DELETE /locales/{localeId}         
PUT /locales/{localeId}    

1. Create a new locale
2. Delete one locale
3. Change the value of one locale



=== LINK ===
POST /links
DELETE /links/{linkId}
PUT /links/{linkId}

1. Create a new link
2. Delete a link
3. Change a link type, value, description, content



=== WORKFLOW ===
POST /workflows
DELETE /workflows/{workflowId}
PUT /workflows/{workflowId}

1. Create a new workflow
2. Delete a workflow
3. Change a workflow name, locale, content, article



=== RELEASE ===
POST /releases

1. Create a new release


=== WorkflowArticle ===
POST /workflowArticles
DELETE /workflowArticles

1. Create an association between a workflow and a page
2. Delete an association between a workflow and a page


=== LinkArticle ===
POST /linkArticles
DELETE /linkArticles

1. Create an association between a link and a page
2. Delete an association between a link and a page
