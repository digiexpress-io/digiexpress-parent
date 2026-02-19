#!/usr/bin/env python3
"""
CSV Update Script: Add TaskComposerV2 annotations to translation keys
Systematically updates all matched translation keys with TaskComposerV2 annotation
"""

import csv
import sys

# All keys that need TaskComposerV2 annotation
KEYS_TO_UPDATE = {
    # Button keys
    'button.accept',
    'button.cancel',
    'button.archive',
    'button.close',
    'button.publish',
    'button.save',
    'button.sendMessage',
    'button.taskReopenDialog',
    'button.cancelSelected',
    'button.acceptSelected',

    # Task status keys
    'task.status',
    'task.status.new',
    'task.status.open',
    'task.status.completed',
    'task.status.rejected',
    'task.status.transferred',
    'task.status.delegated',
    'task.status.waiting',

    # Task priority keys
    'task.priority',
    'task.priority.low',
    'task.priority.normal',
    'task.priority.high',

    # Task audit keys
    'task.audit.commits.commitBody.title',
    'task.audit.commits.author',
    'task.audit.commits.message',
    'task.audit.commits.commitBody',
    'task.audit.commits.createdAt',
    'task.audit.process.type',
    'task.audit.process.value',
    'task.audit.process.createdAt',
    'task.audit.processes.None',
    'task.audit.queue.name',
    'task.audit.queue.createdBy',
    'task.audit.queue.comment',
    'task.audit.queue.createdAt',
    'task.audit.queueBindings.createdBy',
    'task.audit.queueBindings.status',
    'task.audit.queueBindings.comment',
    'task.audit.queueBindings.createdAt',
    'task.audit.queueDeliveries.queueName',
    'task.audit.queueBindings.attempts',
    'task.audit.queueMessages.routingKey',
    'task.audit.queueMessages.bodyType',
    'task.audit.queueMessages.bodyValue',
    'task.audit.queueMessages.createdAt',
    'task.audit.viewers.usedBy',
    'task.audit.viewers.updatedAt',

    # Task composer keys
    'task.composer.error.subject.required',
    'task.composer.create',
    'task.composer.dueDate',
    'task.composer.clientName',
    'task.composer.subject',
    'task.composer.field.required',
    'task.composer.description',
    'task.composer.additionalInfo',
    'task.composer.roles',
    'task.composer.assignee',
    'task.composer.assignments.none',
    'task.composer.priority',
    'task.composer.status',
    'task.composer.task.edit',

    # TaskCard keys (all variants)
    'taskcard.style.COMPACT',
    'taskcard.style.DEFAULT',
    'taskcard.style.LARGE',
    'taskcard.title.rolesAndAssignees',
    'taskcard.title.customerMessages',
    'taskcard.title.notes',
    'taskcard.title.statusAndPriority',
    'taskcard.title.formReview',
    'taskcard.title.history',
    'taskcard.title.taskRefId',
    'taskcard.title.assignable',
    'taskcard.title.customerFeedback',
    'taskcard.title.files',
    'taskcard.body.roles',
    'taskcard.body.assignee',
    'taskcard.body.lastEditedBy',
    'taskcard.body.lastEditedDate',
    'taskcard.body.dueDate',
    'taskcard.body.dueDate.value.invalid',
    'taskcard.body.customerName',
    'taskcard.body.subject',
    'taskcard.body.additionalInfo',
    'taskcard.body.form.formName',
    'taskcard.body.form.submittedDate',
    'taskcard.body.form.canPublishFeedback',
    'taskcard.body.form.representative',
    'taskcard.body.task.status',
    'taskcard.body.task.priority',
    'taskcard.title.audit.viewers',
    'taskcard.title.audit.commits',
    'taskcard.title.audit.queues',
    'taskcard.title.audit.processes',
    'taskcard.title.audit.flow',
    'taskcard.title.audit.queueMessages',
    'taskcard.title.audit.queueBindings',
    'taskcard.title.audit.queueDeliveries',
    'taskcard.button.viewForm',

    # TaskTable keys
    'taskTable.col.header.priority',
    'taskTable.col.header.subject',
    'taskTable.col.header.addInfo',
    'taskTable.col.header.client',
    'taskTable.col.header.status',
    'taskTable.col.header.roles',
    'taskTable.col.header.assignee',
    'taskTable.col.header.due',
    'taskTable.col.header.created',
    'taskTable.col.header.archive',
    'taskTable.col.header.overdue',
    'taskTable.button.archive.confirm',
    'taskTable.confirmArchive.title',
    'taskTable.snackbar.archived',
    'taskTable.snackbar.archiveFailed',
    'taskTable.title',

    # Other task keys
    'task.overdue',
    'task.customerFeedback',
    'task.feedback.none',
    'task.feedback.title',
    'task.feedback.detailedResponse',
    'task.feedback.published',
    'task.feedback.notPublished',
    'task.attachments',
    'task.button.uploadFile',
    'task.file.fileName',
    'task.file.uploadDate',
    'task.files.none',
    'task.files.error.duplicateSingle',
    'task.files.error.duplicateMultiple',
    'task.customerMessages.user.message.wroteOn',
    'task.notes.edit',
    'task.notes.history',
    'task.notes.none',
    'task.notes.newNote',
    'task.status.percComplete',
    'task.priorityAndStatusEdit',
    'task.keywords.internal',
    'task.keywords.customerCreated',
    'task.keywords.protected',
    'task.keywords.normal',
    'taskButton.addTask'
}

def update_csv(input_file, output_file):
    """
    Read CSV, update notes column for matched keys, write back
    """
    rows_updated = 0
    rows_processed = 0

    with open(input_file, 'r', encoding='utf-8') as f_in:
        reader = csv.reader(f_in)
        rows = list(reader)

    # Process each row
    for i, row in enumerate(rows):
        if i == 0:  # Header row
            continue

        if len(row) < 2:  # Skip malformed rows
            continue

        key = row[0]
        notes = row[1] if len(row) > 1 else ''

        # Check if this key needs updating
        if key in KEYS_TO_UPDATE:
            rows_processed += 1

            # Append or add TaskComposerV2 annotation
            if notes and notes.strip():
                # Check if already has TaskComposerV2
                if 'TaskComposerV2' not in notes:
                    rows[i][1] = f"{notes}; TaskComposerV2"
                    rows_updated += 1
            else:
                rows[i][1] = "TaskComposerV2"
                rows_updated += 1

    # Write updated CSV
    with open(output_file, 'w', encoding='utf-8', newline='') as f_out:
        writer = csv.writer(f_out)
        writer.writerows(rows)

    return rows_processed, rows_updated

if __name__ == '__main__':
    input_csv = '/Users/jocelyn/Development/projects/digiexpress-parent/ts_setup/eveli-ide/intl.csv'
    output_csv = '/Users/jocelyn/Development/projects/digiexpress-parent/ts_setup/eveli-ide/intl.csv'

    print(f"Processing CSV: {input_csv}")
    print(f"Keys to update: {len(KEYS_TO_UPDATE)}")

    processed, updated = update_csv(input_csv, output_csv)

    print(f"\n✓ Processing complete!")
    print(f"  - Keys matched: {processed}")
    print(f"  - Keys updated: {updated}")
    print(f"  - Output written to: {output_csv}")
