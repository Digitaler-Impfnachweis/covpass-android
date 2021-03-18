#!/usr/bin/env groovy
@Library('declarative-jenkins-steps@v-1.143.0') _

pipeline {
    agent {
        node {
            label 'Android'
        }
    }
    tools {
        jdk 'jdk_8'
    }
    options {
        disableConcurrentBuilds()
        skipDefaultCheckout()
    }
    triggers {
        issueCommentTrigger('.*clean build please.*')
    }
    stages {
        stage('CleanWs') {
            when {
                anyOf {
                    expression { return buildWasTriggeredByIssueComment() }
                    branch 'master'
                    branch 'release/*'
                    branch 'snapshot/*'
                }
            }
            steps {
                cleanWs()
            }
        }
        stage('Checkout') {
            steps {
                commonCheckout()
            }
        }
        stage('PR Checks') {
            when {
                anyOf {
                    branch 'PR-*'
                }
            }
            steps {
                script {
                    if (env.CHANGE_ID) {
                        // TODO: re-enable checks
                        def shouldIgnoreChecks = true || pullRequest.labels.contains("ignore-pr-naming")
                        if (!shouldIgnoreChecks && !(pullRequest.headRef ==~ /[\w_\-]+\/(bump.*|revert.*|refactor.*|[A-Z]{3,}-\d+(,[A-Z]{3,}-\d+)*(([_\-]).+)?)/)) {
                            error("PR branch name ${pullRequest.headRef} doesn't start with ticket ID after first slash")
                        }

                        if (!shouldIgnoreChecks && !(pullRequest.title ==~ /[Bb]ump.*|[Rr]evert.*|[Rr]efactor.*|[A-Z]{3,}-\d+(,\s*?[A-Z]{3,}-\d+)*:?\s+.+/)) {
                            error("PR title doesn't start with \"Bump\" or ticket ID (e.g. \"EBH-12345: bla bla\" or \"EBH-12345, EBH-23456: bla bla for two tickets\")")
                        }

                        setLabelForPattern(pullRequest, 'dependencies-changed', /dependencies\.gradle/, null)
                        setLabelForPattern(pullRequest, 'gradle-common', /gradle\/common/, null)
                        setLabelForPattern(pullRequest, 'proguard', /(proguard|consumer)-rules.*\.pro/, null)
                        setLabelForPattern(pullRequest, 'security-critical', /(security|http|oidc).*/, null)
                    }
                }
            }
        }
        stage('Create Release') {
            when {
                anyOf {
                    branch 'master'
                    branch 'release/*'
                }
            }
            steps {
                script {
                    gradleCreateRelease()
                }
            }
        }
        stage('Document Snapshot') {
            when {
                anyOf {
                    branch 'snapshot/*'
                }
            }
            steps {
                script {
                    currentBuild.displayName = gradleStdout('currentVersion', '-q').substring(17)
                }
            }
        }
        stage('Checks & Assemble') {
            parallel {
                stage('Kotlin Lint') {
                    steps {
                        gradle('ktlint')
                    }
                    post {
                        always {
                            recordIssues(tools: [checkStyle(id: 'ktlint', name: 'ktlint', pattern: '**/build/ktlint.xml')])
                        }
                        failure {
                            script {
                                // If this is a PR, push an automatic ktlintFormat self-cleanup to the original branch
                                if (env.CHANGE_ID) {
                                    gradle('ktlintFormat')
                                    sh('git fetch')
                                    sh("git checkout '${env.CHANGE_BRANCH}'")
                                    sh("git commit -a -m 'Katy magic'")
                                    sh("git push")
                                }
                            }
                        }
                    }
                }
                stage('Detekt') {
                    steps {
                        gradle('detekt')
                    }
                    post {
                        always {
                            recordIssues(tools: [checkStyle(id: 'detekt', name: 'detekt', pattern: "**/build/reports/detekt/detekt.xml")])
                        }
                    }
                }
                stage('Assemble') {
                    steps {
                        gradleAssemble('assemble')
                    }
                }
            }
        }
        stage('Tests & Lint') {
            parallel {
                stage('Android Lint') {
                    steps {
                        gradle('lint')
                    }
                }
                stage('Dependency Track') {
                    when {
                        anyOf {
                            branch 'master'
                            branch 'release/*'
                        }
                    }
                    steps {
                        // masterScan:"latest" -> suppress reporting of each built version, only report as "#latest" on master branches
                        // cycloneDX plugin for other Android projects so far, otherwise it would fail during execution with submodule dependencies.
                        // releaseVersions:"prefix" -> add the release branch name to the displayed version for better identification
                        // To be removed, after the current configuration here is applicable to the other Android projects.
                        dependencyTrack(masterScan:'latest', releaseVersions:'prefix', maxWorkers:2)
                    }
                }
                stage('Log Dependencies') {
                    steps {
                        script {
                            sh('python3 extract-dependencies.py > dependencies.txt')
                            archiveArtifacts 'dependencies.txt'
                        }
                    }
                }
                stage('Unit Tests') {
                    steps {
                        gradle('testDebugUnitTest')
                    }
                }
            }
        }
        stage('Verification') {
            steps {
                sh 'touch **/build/test-results/**/* || true'
                junit '**/build/test-results/**/*.xml'

                gradle('jacocoTestReportDefault')
                // Ignore coverage for some modules
                sh 'rm -rf android-utils-test/build/reports/jacoco'
                jacocoReport('', 9.0, true, 'jacocoTestReportDefault', true)
            }
        }
// TODO: Add documentation
//        stage('Documentation') {
//            when {
//                anyOf {
//                    branch 'master'
//                    branch 'release/*'
//                    branch 'snapshot/*'
//                }
//            }
//            steps {
//                javadocAndroid()
//            }
//        }
        stage('Publish Release') {
            when {
                anyOf {
                    branch 'master'
                    branch 'release/*'
                }
            }
            steps {
                script {
                    // Prevent conflicts for parallel builds
                    sh('git fetch --tags')

                    // Archive mappings
                    sh 'zip -r mappings.zip */build/outputs/mapping/'
                    archiveArtifacts 'mappings.zip'

                    // Check if we have any tags to push.
                    def toPush = sh(
                        returnStdout: true,
                        script: "git push --dry-run --porcelain --tags | grep '^*' || [ \$? -eq 1 ]"
                    ).trim()

                    // Add extra tag for release branches, so we can track them even when doing fast-forward merges.
                    if (env.BRANCH_NAME != "master") {
                        def prefix = env.BRANCH_NAME.replaceAll(/[^\/a-zA-Z0-9_\-]+/, '-')
                        def version = currentBuild.displayName
                        sh("git tag $prefix-$version || true")
                    }

                    // Only publish release if tag doesn't exist, yet.
//                    if (toPush != "") {
//                        withEnv(["PUBLISH_SOURCES=true"]) {
//                            gradle('publish', '--stacktrace')
//                        }
//                        withEnv(["PUBLISH_SOURCES=false"]) {
//                            gradle('publish', '--stacktrace')
//                        }
//                    }
                }
                finishRelease()
            }
        }
    }
    post {
        failure {
            script {
                if (env.CHANGE_BRANCH =~ 'master|release/.*') {
                    slackNotifyBuildFailed('#android_community')
                }
            }
        }
    }
}

def setLabelForPattern(pullRequest, String label, pattern, diffPattern = null) {
    if (pullRequest.files.find { f -> f.filename =~ pattern } != null ||
            diffPattern != null &&
            pullRequest.commits.find { c -> c.files { f -> f.patch =~ diffPattern } != null } != null) {
        if (!pullRequest.labels.contains(label)) {
            pullRequest.addLabel(label)
        }
    } else if (pullRequest.labels.contains(label)) {
        pullRequest.removeLabel(label)
    }
}
