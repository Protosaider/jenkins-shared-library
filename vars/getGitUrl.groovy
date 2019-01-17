#!/usr/bin/env groovy

def call() {
    def url = sh(returnStdout: true, script: "git remote get-url origin").trim()
    return url
}
