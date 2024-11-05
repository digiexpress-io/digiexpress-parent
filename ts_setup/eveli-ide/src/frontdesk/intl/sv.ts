const sv = {

  //----------------START NEW-------------------------
  'explorer.title': 'DigiExpress Task Management',
  'explorer.logout': 'Logga ut',
  'explorer.feedback': 'Skicka feedback',

  // ---------------END NEW --------------------------


  'locale.fi': 'Finska',
  'locale.sv': 'Svenska',
  'locale.en': 'Engelska',

  'app.logout': 'Logga ut',
  'app.login': 'Logga in',
  'app.feedback': 'Respons',

  'menu.dashboard': 'Grunduppgifter',
  'menu.profile': 'Användarens profil',
  'menu.tasks': 'Tjänst',
  'menu.evaluations': 'Förfrågningar',
  'menu.forms': 'Dialob',
  'menu.users': 'Användare',
  'menu.help': 'Anvisningar',
  'menu.flow': 'Wrench',
  'menu.processes': 'Övervakning',
  'menu.reports': 'Rapportering',
  'menu.workflows': 'Konfigurera',
  'menu.workflowTags': 'Arbetsflödestaggar',
  'menu.publications': 'Offentliggörande',
  'menu.services': 'Tjänster',
  'menu.content': 'Stencil',
  'menu.calendar': 'Kalender',

  'front.intro.title': 'Välkommen!',
  
  'profile.title': 'Användarprofil',
  'profile.user.name': 'Namn:',
  'profile.user.email': 'e-post:',
  'profile.user.role': 'Roll:',
  'profile.user.phone': 'Telefon:',
  'profile.app.version': "Applikationsversion:",

  'button.cancel' : 'Avbryt',
  'button.accept' : 'Acceptera',
  'button.edit' : 'Öppna',
  'button.close' : 'Stäng',
  'button.reject' : 'Avslå',
  'button.send': 'Skicka',
  'button.dismiss': 'Stäng',
  'button.editRoles': 'Redigera',
  'button.select': 'Select',

  'error.minTextLength': 'Texten måste vara minst {minLength} tecken lång.',
  'error.maxTextLength': 'Texten måste vara max {max} tecken lång.',
  'error.valueRequired': 'Nödvändig',
  'error.title': 'Fel!',
  'error.dialob.session': 'Blanketten kan inte öppnas just nu. Vänligen försök på nytt senare',
  'error.errorLoadingData': 'Fel då försöker ladda data',
  'error.unauthorized.title': 'Du saknar rättighet till tjänsten',
  'error.unauthorized.text': 'Tag kontakt med IT genom att göra en Tiket: Rapportera IT problem -> Problem med inloggning.',
  'error.unauthorizedAccess': 'Du saknar rättighet till tjänsten',
  'error.dataAccess': 'Fel vid drift',
  'error.statusOpenError': 'Required when status is open',
  'error.workflowCreation': 'Error on workflow creation, check worflow name for duplicates',

  'table.body.addTooltip': 'Lägg till',
  'table.body.editTooltip': 'Öppna',
  'table.body.editRow.deleteMessage': 'Är du säker på att du vill radera detta?',

  'workflowTable.title': 'Sammansättning',
  'workflowTableHeader.name': 'Namn',
  'workflowTableHeader.formName': 'Blankett',
  'workflowTableHeader.flowName': 'Process',
  'workflowTableHeader.updated': 'Uppdaterad',
  'workflowTable.addButton': 'Lägg till ny',
  'workflowTable.editButton': 'Editera',

  'workflow.dialogTitle': 'Nytt arbetsflöde',
  'workflow.name': 'Namn',
  'workflow.form.formName': 'Blankettens namn',
  'workflow.form.formTag': 'Blankettens version',
  'workflow.flowName': 'Process',

  'processTable.title': 'Arbeten',
  'processTableHeader.workflow': 'Typ',
  'processTableHeader.user': 'Användare',
  'processTableHeader.status': 'Status',
  'processTableHeader.created': 'Skapad',
  'processTableHeader.questionnaireId': 'Blankett',

  'process.status.ANSWERED': 'Skickad',
  'process.status.CREATED': 'Skapad',
  'process.status.ANSWERING': 'På hälft',
  'process.status.IN_PROGRESS': 'Under arbete',
  'process.status.WAITING': 'Väntar',
  'process.status.COMPLETED': 'Färdig',
  'process.status.REJECTED': 'Avslagen',

  'comment.button': 'Ge respons',
  'comment.title': 'Ge respons',
  'comment.form.email': 'E-post',
  'comment.form.feedbackType': 'Typ',
  'comment.form.body': 'Meddelande',
  'comment.type.general': 'Ge respons',
  'comment.type.propose': 'Föreslå ny tjänst',
  'comment.type.contact': 'Tag kontakt',
  'comment.submitted.success': 'Tack för din respons!',
  'comment.submitted.failure': 'Överföringen lyckades inte!',

  'spoTasksTableHeader.taskName': 'Uppgiftens namn',
  'spoTasksTableHeader.category': 'Typ',
  'spoTasksTableHeader.status': 'Status',
  'spoTasksTableHeader.priority': 'Prioritet',
  'spoTasksTableHeader.dueDate': 'Förfallodag',
  'spoTasksTableHeader.assigned': 'Hanterare',
  'spoTasksTableHeader.assignedUser': 'Användare',
  'spoTasksTableHeader.created': 'Ankomnstdatum',
  'spoTasksTableHeader.clientName': 'Kund',

  'taskDialog.clientIdentificator': 'Kundens namn',
  'taskDialog.assignedUserEmail': 'Användarens e-post',
  'task.editRoles': 'Arbetsgrupper',

  // for task labels
  'Questionnaire': 'Blankett',
  'Manual': 'Manuellt',
  'Protected': 'Skyddad',
  'Normal': 'Normalt',
  'Represented': 'Representerade',
  "CustomerCreated": "Kund skapad",
  "Internal": "Intern",

  'task.role.assignedAllUsers': 'Alla användare',

  'confirm.close.title': 'Bekräftelse krävs',
  'confirm.unsavedChanges': 'Osparade ändringar kommer att gå förlorade, vill du fortsätta?',

  'task.statistics.statusCount': 'Efter status',
  'task.statistics.priorityCount': 'Efter prioritet',
  'task.statistics.daily': 'Dagligen',
  'task.statistics.overdue': 'Försenad',

  'feedback.button': 'Skicka respons',
  'feedback.thanks': 'Tack för din respons!',
  'feedback.title': 'Skicka respons',
  'feedback.description': 'Beskrivning',
  'feedback.name': 'Ditt namn',
  'feedback.sendScreenshot': 'Skicka en skärmdump',
  'feedback.close': 'Avbryt',
  'feedback.send': 'Skicka',

  'help.title': 'Anvisningar',

  'attachmentView.title': 'Attachments',
  'attachmentTableHeader.size': 'Size',
  'attachmentTableHeader.updated': 'Updated',
  'attachmentTableHeader.created': 'Created',
  'attachmentTableHeader.name': 'Name',
  'attachmentButton.addAttachment': 'Add file',
  'attachmentButton.downloadAttachment': 'Download file',
  'attachment.uploadOk': 'File {fileName} uploaded',
  'attachment.uploadFailed': 'File {fileName} upload failed!',

  'workflowReleaseTable.title': 'Workflow release',
  'workflowReleaseTableHeader.name': 'Name',
  'workflowReleaseTableHeader.description': 'Description',
  'workflowReleaseTableHeader.updatedBy': 'Updater',
  'workflowReleaseTableHeader.updated': 'Updated',
  'workflowReleaseTable.addButton': 'Add',
  'workflowReleaseTable.viewButton': 'View',
  'workflowReleaseTable.exportButton': 'Export',
  
  'workflowRelease.dialogTitle': 'View workflow release',
  'workflowRelease.name': 'Release name',
  'workflowRelease.description': 'Description',
  'workflowRelease.releaseCreationFailed': 'Workflow tag creation failed, cause: {cause}',

  'publicationsTable.title': 'Release',
  'publicationsTableHeader.name': 'Release name',
  'publicationsTableHeader.description': 'Description',
  'publicationsTableHeader.createdBy': 'User',
  'publicationsTableHeader.created': 'Created',
  'publicationsTableHeader.contentTag': 'Content tag',
  'publicationsTableHeader.workflowTag': 'Workflow tag',
  'publicationsTableHeader.wrenchTag': 'Wrench tag',
  'publicationsTable.addButton': 'Add',
  'publicationsTable.viewButton': 'View',
  'publicationsTable.exportButton': 'Export',

  'publications.dialogTitle': 'Release',
  'publications.name': 'Release name',
  'publications.description': 'Description',
  'publications.workflowTag': 'Workflow tag',
  'publications.wrenchTag': 'Wrench tag',
  'publications.contentTag': 'Content tag',
  'publications.createNewTag': 'Create new tag "{tag}"',
  'publications.downloadFailed': 'Release download failed, cause: {cause}',
  'publications.tagCreationFailed': 'Release creation failed, cause: {cause}',

  'dialobForms.dialog.heading': 'Forms',
  'dialobForms.table.tooltip.add': 'Add',
  'dialobForms.table.label': 'ID',
  'dialobForms.table.created': 'Created',
  'dialobForms.table.lastSaved': 'Updated',
  'dialobForms.table.tooltip.edit':'Edit',
  'dialobForms.dialog.emptyTitle':'-',
  'dialobForms.table.tooltip.copy':'Copy',
  'dialobForms.table.tooltip.delete':'Remove',
  'dialobForm.saveFailed': 'Saving failed',
  'dialobForm.downloadFailed':'Saving failed',
  'dialobForm.deleteFailed': 'Remove failed',
  'dialobForm.heading.copyDialog':'Copy',
  'dialobForm.heading.addDialog':'Add',
  'dialobForm.dialog.formName':'ID',
  'dialobForm.dialog.formLabel':'Name',
  'dialobForm.error.invalidFormName': 'Incorrect ID',
  'dialobForm.dialog.deleteQuestion': 'Do you want to remove form "{formName}"?',
  'dialobForm.heading.deleteDialog':'Remove form',
};

export default sv;
