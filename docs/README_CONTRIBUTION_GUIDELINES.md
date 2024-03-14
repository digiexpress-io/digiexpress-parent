# Contribution guidlines

## Contents

1. Branch organisation, feature branch requirements, pull request requirements
2. Writing issues
3. Making a feature branch
4. Committing changes
5. Making a pull request

### Branch Organization

* Development happens on individual feature branches. 
* Feature branches are created from the `dev` branch.  
* Pull requests are made to the `dev` branch.
* Feature branches are deleted after PR is complete.

### Feature branch requirements

Feature branches must
* Contain ONLY the changes related to the current issue being worked on
* Be linked to the Github issue (can be done via the Github UI on an existing issue)

### Pull request requirements

* All pull requests must include **Screenshots** or a screen capture + short written description of the changes included in the PR. 
* All pull requests will undergo a code review. If changes are requested, they must be completed before the PR will be merged.

Pull requests will be refused if
* The PR feature branch was created from a different feature branch instead of the `dev` branch
* The code is not of acceptable quality (i.e. cognitive complexity, naming issues)
* The code introduces new bugs
* Screenshots / screen capture is not included (when applicable)
* The code contains changes unrelated to the issue at hand

---

# Follow these steps from issue creation to pull request

## Step 1: Write an issue

Issues are written in the following format:

| field                                   | description                                                                                                 |
|:----------------------------------------|:------------------------------------------------------------------------------------------------------------|
| Issue title                             |  **[UI]** + **Path to the folder** in which you are working + **short description** of the work to be done  |
| Issue description                       |  **Description:** Describe the purpose of this feature                                                      |
|                                         |  **Expected outcome:** Describe the end result of this work                                                 |
| Assignees                               |  Assign **yourself** upon issue creation                                                                    |
| Labels                                  |  Use Labels whenever applicable                                                                             |
| Projects                                |  Assign issue to a Project. Confirm project with the team lead.                                             |
| Milestone                               |  -                                                                                                          |
| Development                             |  Use this option to link your feature branch to the issue.                                                  |

## Step 2: Make a feature branch 

* The simplest way to make a new feature branch is via the Github UI from the issue itself.
* Go to the Development section on the issue and select "Create a branch".  

## Step 3: Commit changes and include issue numbers

* To keep a history of the changes associated with an issue, include the issue number in your commit messages.
* In the example below, this commit message is referencing issue number 3.

```bash
git commit -am "#3 fixed readme"
```

## Step 4: Make a pull request

* Create a new merge request into `dev` branch only!
* Mark the merge request as a Draft until it is ready for review.
* Assign a reviewer







