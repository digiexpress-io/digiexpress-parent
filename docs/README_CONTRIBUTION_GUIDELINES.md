# Contribution guidlines

## Contents

1. Branch organisation, Feature branch requirements, pull request requirements
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

Issues are written in the following format via the Github issue creation UI.

| field                                   | description                                                                                      |
|:----------------------------------------|:-------------------------------------------------------------------------------------------------|
| Issue title                             |  **Path to the folder** in which you are working + **short description** of the work to be done  |
| Issue description                       |  **Introduction:** Short description of the reasons why this issue is needed.                    |
|                                         |  **Expected outcome:** A list/description of the final result of this work                       |
| Assignees                               |  Assign **yourself** upon issue creation                                                         |
| Labels                                  |  Use Labels whenever applicable                                                                  |
| Projects                                |  The issue must be assigned to a Project. Confirm project with the team lead.                    |
| Milestone                               |  -                                                                                               |
| Development                             |  Use this menu option to link your feature branch to the issue.                                  |

## Step 2: Make a feature branch 

* The simplest way to make a new feature branch is via the Github UI from the issue itself.
* From your issue, go to the Development section on the right side of the screen and select "Create a branch".  

## Step 3: Commit changes and include issue numbers

* To keep a history of the changes associated with an issue, include the issue number in your commit messages.
* In the example below, this commit message is referencing issue number 3.

```bash
git commit -am "#3 fixed readme"
```

## Step 4: Make a pull request

* Create a new merge request into `dev` branch only!
* Mark the merge request as a Draft until it is ready for review.







