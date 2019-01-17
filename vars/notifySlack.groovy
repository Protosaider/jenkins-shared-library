#!/usr/bin/env groovy

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

def call(String buildStatus = 'STARTED', String channel = '#build') {

  long epoch = System.currentTimeMillis()/1000

  // Build status of null means success.
  buildStatus = buildStatus ?: 'SUCCESS'

  def color

  if (buildStatus == 'STARTED') {
      color = '#D4DADF'
      // color = 'good'
  } else if (buildStatus == 'SUCCESS') {
      color = '#BDFFC3'
      // color = 'good'
  } else if (buildStatus == 'UNSTABLE') {
      color = '#FFFE89'
      // color = 'warning'
  } else if (status == 'ABORTED') {
      color = '#FFFE89'
      // color = 'warning'
  } else if (status == 'NOT_BUILT') {
      color = '#FFFE89'
      // color = 'warning'
  }
  else if (buildResult == 'FAILURE') {
      color = '#FFFE89'
      // color = 'danger'
  } 
  else {
      color = '#FF9FA1'
  }

  // get Jenkins user that has started the build
  wrap([$class: 'BuildUser']) { script { env.USER_ID = "${BUILD_USER_ID}" } }

  def subject = "${buildStatus}: Job ${env.JOB_NAME} build #${env.BUILD_NUMBER} by ${env.USER_ID}"

  def msg = "${subject}\n More info at: ${env.BUILD_URL}"

  JSONArray attachments = new JSONArray();
  JSONObject attachment = new JSONObject();

  attachment.put('fallback', '${subject}');
  attachment.put('pretext', 'Git info');
  attachment.put('color', '$(color)');

  JSONObject fieldBranch = new JSONObject();
  fieldBranch.put('title', 'Branch');
  fieldBranch.put('value', '${env.BRANCH_NAME}');
  fieldBranch.put('short', 'true');

  JSONObject fieldGitAuthor = new JSONObject();
  fieldGitAuthor.put('title', 'Author');
  def author = getGitAuthor();
  fieldGitAuthor.put('value', '${author}');
  fieldGitAuthor.put('short', 'true');

  JSONObject fieldLastCommitMessage = new JSONObject();
  fieldLastCommitMessage.put('title', 'Last commit');
  def lastCommitMessage = getLastCommitMessage();
  fieldLastCommitMessage.put('value', '${lastCommitMessage}');
  fieldLastCommitMessage.put('short', 'true');

  JSONArray fields = new JSONObject();
  fields.add(fieldBranch);
  fields.add(fieldGitAuthor);
  fields.add(fieldLastCommitMessage);

  attachment.put('fields', fields);

  attachments.add(attachment);

  slackSend(color: color, message: msg, channel: channel, attachments: attachments.toString())


  // // first of all, notify the team
  // slackSend (color: "${env.SLACK_COLOR_INFO}",
  //            channel: "${params.SLACK_CHANNEL_1}",
  //            message: "*STARTED:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} by ${env.USER_ID}\n More info at: ${env.BUILD_URL}",
  //            )      

  // // Create JSON Payload
  // def json = JsonOutput.toJson(
  //     [
  //         attachments: [[
  //             title: "Job: ${env.JOB_NAME}, build #${env.BUILD_NUMBER}",
  //             title_link: "${env.BUILD_URL}",
  //             fallback: "Jenkins Job Alert - ${buildStatus} - ${env.JOB_NAME}", 
  //             color: color,
  //             fields: [[
  //                 title: "Repository",
  //                 value: "<${insert SCM Host}/${env.GIT_REPO_NAME}|${env.GIT_REPO_NAME}>",
  //                 short: true
  //             ],[
  //                 title: "Branch",
  //                 value: "${env.GIT_BRANCH}",
  //                 short: true
  //             ],[
  //                 title: "Last Commit",
  //                 value: "${message}",
  //                 short: false
  //             ]],
  //             footer: "<@${env.BUILD_USER_ID}>",
  //             ts: epoch,
  //             mrkdwn_in: ["footer", "title"],
  //         ]]
  //     ]
  // )
}