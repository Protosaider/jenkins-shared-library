#!/usr/bin/env groovy

def call(String buildStatus = 'STARTED') {

  def subject = "${buildStatus}: Job `${env.JOB_NAME}` with build number #${env.BUILD_NUMBER}:"

  def details = """<p>${subject}</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""

  emailext (
      to: 'protosaider@gmail.com',
      subject: subject,
      body: details,
      recipientProviders: [[$class: 'DevelopersRecipientProvider']]
    )
}