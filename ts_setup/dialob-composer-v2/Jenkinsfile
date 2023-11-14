#!groovy
pipeline {
  agent any
  tools {
    nodejs 'nodejs-12'
  }
  options {
    gitLabConnection('git.resys.io')
    gitlabCommitStatus(name: 'jenkins')
    ansiColor('xterm')
    buildDiscarder(logRotator(numToKeepStr: '5'))
    disableConcurrentBuilds()
    skipStagesAfterUnstable()
  }
  triggers {
    gitlab(triggerOnPush: true, triggerOnMergeRequest: false, branchFilterType: 'All')
  }
  environment {
    DOCKER_TAG = "$BRANCH_NAME"
    PROJECT_NAME = "dialob-composer"
  }

  stages {
    /*
    stage('Init') {
      steps {

      }
    }
    */

    stage('Test') {
      steps {
        sh 'node -v'
        sh 'yarn install'
      //  sh 'yarn test' // Temporarily disabled test
      }
    }

    stage('Build') {
      steps {
       // sh 'git checkout -- package-lock.json'
        sh 'yarn version --prerelease'
        sh 'yarn build'
        sshagent(credentials: ['f41971b1-4bca-4f7c-91a4-9fd6f46e0df3']) {
          sh "git push origin HEAD:$BRANCH_NAME && git push --tags origin HEAD:$BRANCH_NAME"
        }
      }
    }

    stage('Publish') {
      steps {
        // Yarn 1.6 will respect publishConfig repositories...
        sh 'npm publish'
      }
    }
  }

/*
 post {
    failure {
      slackSend "${env.JOB_NAME} - ${env.BUILD_DISPLAY_NAME} Failure (<${env.BUILD_URL}|Open>)"
    }
    unstable {
      slackSend "${env.JOB_NAME} - ${env.BUILD_DISPLAY_NAME} Unstable (<${env.BUILD_URL}|Open>)"
    }
    success {
      slackSend "${env.JOB_NAME} - ${env.BUILD_DISPLAY_NAME} Success (<${env.BUILD_URL}|Open>)"
    }
  }
*/
}
