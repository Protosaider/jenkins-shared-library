#!/usr/bin/env groovy

import groovy.json.JsonOutput

def slackNotificationChannel = 'build' // ex: = "builds"

def call(text, channel, attachment) {

  // def slackURL = '[SLACK_WEBHOOK_URL]'
  def slackURL = '[SLACK_WEBHOOK_URL]'
  def jenkinsIcon = 'https://wiki.jenkins-ci.org/download/attachments/2916393/logo.png'

  def payload = JsonOutput.toJson([text: text,
      channel: channel,
      username: "Jenkins",
      icon_url: jenkinsIcon,
      attachments: attachments
  ])

  sh "curl -X POST --data-urlencode \'payload=${payload}\' ${slackURL}"
}

def getGitAuthor = {
    def commit = sh(returnStdout: true, script: 'git rev-parse HEAD')
    def author = sh(returnStdout: true, script: "git --no-pager show -s --format='%an' ${commit}").trim()
    return author
}

def getLastCommitMessage = {
    def message = sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim()
    return message
}

// def getLastTag = {
//     def tag = sh(returnStdout: true, script: 'git describe --abbrev=0 --tags')
//     return tag
// }



def sendBuildNotification(sharedVars, String buildStatus = 'STARTED', String slackHeading = 'BUILD') {

    // Build status of null means success.
    buildStatus = buildStatus ?: 'SUCCESS'
    // Default image is normal Jenkins
    image =':jenkins:'
    long epoch = System.currentTimeMillis()/1000

    // Determine Color
    def color
    if (buildStatus == 'STARTED' || buildStatus == 'ABORTED') {
        color = '#D4DADF' // Grey
    } else if (buildStatus == 'SUCCESS') {
        color = '#319b20' // Green
    } else if (buildStatus == 'UNSTABLE' || buildStatus == 'INPUT REQUIRED') {
        color = '#ff8316' // Yellow
    } else {
        color = '#f44242' // Red
        image=':badjenkins:'
    }

    // Create JSON Payload
    def json = JsonOutput.toJson(
        [
            username: "${slackHeading} ${buildStatus}",
            channel: "${insertChannel}",
            icon_emoji: image,
            attachments: [[
                title: "Job: ${env.JOB_NAME}/${env.BUILD_NUMBER}",
                title_link: "${env.BUILD_URL}console",
                fallback: "Jenkins Job Alert - ${buildStatus} - ${env.JOB_NAME}", 
                color: color,
                fields: [[
                    title: "Repository",
                    value: "<${insert SCM Host}/${env.GIT_REPO_NAME}|${env.GIT_REPO_NAME}>",
                    short: true
                ],[
                    title: "Branch",
                    value: "<${insert SCM Host}/${env.GIT_REPO_NAME}/tree/${BRANCH_NAME}|${BRANCH_NAME}>",
                    short: true
                ],[
                    title: "Last Commit",
                    value: "<${insert SCM Host}/${env.GIT_REPO_NAME}/commit/${sharedVars.gitCommit}|${sharedVars.gitCommit}>",
                    short: false
                ]],
                footer: "<@${env.BUILD_USER_ID}>",
                ts: epoch,
                mrkdwn_in: ["footer", "title"],
            ]]
        ]
    )

    try {
        // Post to Slack
        sh "curl -X POST -H 'Content-type: application/json' --data '${json}' ${slackURL} --max-time 5"
    } catch (err) {
        echo "${err} Slack notify failed, moving on"
    }
}