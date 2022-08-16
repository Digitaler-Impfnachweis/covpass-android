#!/usr/bin/env groovy

/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

SECRETS = [
    [path: 'secret/eGA/tools/firebase', secretValues: [[vaultKey: 'token', envVar: 'FIREBASE_CLI_TOKEN']]],
    [path: 'secret/eGA/tools/firebase', secretValues: [[vaultKey: 'FIREBASE_COVPASS_ID', envVar: 'FIREBASE_COVPASS_ID']]],
    [path: 'secret/eGA/tools/firebase', secretValues: [[vaultKey: 'FIREBASE_COVPASS_CHECK_ID', envVar: 'FIREBASE_COVPASS_CHECK_ID']]],
    [path: 'secret/eGA/tools/sauce', secretValues: [[vaultKey: 'SAUCE_USERNAME', envVar: 'SAUCE_USERNAME']]],
    [path: 'secret/eGA/tools/sauce', secretValues: [[vaultKey: 'SAUCE_ACCESS_KEY', envVar: 'SAUCE_ACCESS_KEY']]],
]

pipeline {
    agent {
        node {
            label 'Android'
        }
    }
    parameters {
        choice(name: 'Firebase', choices: ['false','true'], description: 'Release on firebase')
    }
    tools {
        jdk 'jdk_11_hotspot'
    }
    options {
        disableConcurrentBuilds()
        skipDefaultCheckout()
        timeout(time: 1, unit: 'HOURS') // safeguard to auto-kill stuck builds
        ansiColor("xterm") // needs AnsiColor plugin (https://wiki.jenkins.io/display/JENKINS/AnsiColor+Plugin)
    }
    triggers {
        issueCommentTrigger('.*clean build please.*')
    }
    stages {
        stage('CleanWs') {
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
                        def shouldIgnoreChecks = pullRequest.labels.contains("ignore-pr-naming")
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
        stage('Version code') {
            steps {
                script {
                    sh('./write-version-code.sh')
                }
            }
        }
        stage('Create Release') {
            when {
                anyOf {
                    branch 'main'
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
        stage('Assemble Debug') {
            steps {
                // Running licenseReleaseReport in parallel causes bugs, so we run serially.
                withCredentials([usernamePassword(credentialsId: 'github_account_ibm-ihc-dev', usernameVariable: 'GITHUB_PACKAGES_USERNAME', passwordVariable: 'GITHUB_PACKAGES_PASSWORD')]) {
                    sh('for app in app-*; do ./gradlew $app:licenseReleaseReport; done')
                    sh('./gradlew assembleDebug')
                }
            }
        }
        stage('Android Lint') {
            steps {
                gradle('lint')
            }
        }
        stage('Dependency Track') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'release/*'
                }
            }
            steps {
                // masterScan:"latest" -> suppress reporting of each built version, only report as "#latest" on main/master branches
                // cycloneDX plugin for other Android projects so far, otherwise it would fail during execution with submodule dependencies.
                // releaseVersions:"prefix" -> add the release branch name to the displayed version for better identification
                // To be removed, after the current configuration here is applicable to the other Android projects.
                dependencyTrack(masterScan:'latest', releaseVersions:'prefix', maxWorkers:2)
            }
        }
        stage('Log Dependencies') {
            steps {
                script {
                    sh('./gradlew depsTree > dependencies-tree.txt')
                    sh('python3 extract-dependencies.py --modules APP > dependencies-app.txt')
                    sh('python3 extract-dependencies.py > dependencies-sdk-with-test-modules.txt')
                    sh('python3 extract-dependencies.py --exclude-modules TEST > dependencies-sdk-without-test-modules.txt')
                    sh('python3 extract-dependencies.py --configurations ALL > dependencies-including-tests.txt')
                    archiveArtifacts 'dependencies*.txt'
                }
            }
        }
        stage('Unit Tests') {
            steps {
                gradle('testDebugUnitTest')

                sh 'touch **/build/test-results/**/* || true'
                junit '**/build/test-results/**/*.xml'

                gradle('jacocoTestReportDefault')
                // Ignore coverage for some modules
                sh 'rm -rf android-utils-test/build/reports/jacoco'
                sh 'rm -rf common-app/build/reports/jacoco'
                jacocoReport('', 22.0, true, 'jacocoTestReportDefault', true)
            }
        }
        stage('Assemble Release') {
            // This is only needed when publishing below, so we select published branches only.
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'release/*'
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'github_account_ibm-ihc-dev', usernameVariable: 'GITHUB_PACKAGES_USERNAME', passwordVariable: 'GITHUB_PACKAGES_PASSWORD')]) {
                    gradle('assembleRelease')
                }

                script {
                    withDockerRegistry(registry: [url: 'https://de.icr.io/v2/', credentialsId: 'icr_image_puller_ega_dev_api_key']) {
                        withCredentials([
                            file(credentialsId: 'release_vaccinee', variable: 'RELEASE_KEYSTORE'),
                            string(credentialsId: 'release_vaccinee_password', variable: 'RELEASE_KEYSTORE_PASSWORD'),
                        ]) {
                            sh "./run-in-docker.sh ./sign.sh app-covpass-demo"
                        }
                        withCredentials([
                            file(credentialsId: 'release_verification', variable: 'RELEASE_KEYSTORE'),
                            string(credentialsId: 'release_verification_password', variable: 'RELEASE_KEYSTORE_PASSWORD'),
                        ]) {
                            sh "./run-in-docker.sh ./sign.sh app-covpass-check-demo"
                        }
                    }
                }
            }
        }
// TODO: Add documentation
//        stage('Documentation') {
//            when {
//                anyOf {
//                    branch 'main'
//                    branch 'master'
//                    branch 'release/*'
//                    branch 'snapshot/*'
//                }
//            }
//            steps {
//                javadocAndroid()
//            }
//        }
        stage('Play Store') {
            when {
                anyOf {
                    branch 'release/play-store'
                }
            }
            steps {
                script {
                    withDockerRegistry(registry: [url: 'https://de.icr.io/v2/', credentialsId: 'icr_image_puller_ega_dev_api_key']) {
                        withCredentials([
                            file(credentialsId: 'internal-supply-key', variable: 'SUPPLY_JSON_KEY'),
                        ]) {
                            sh "./run-in-docker.sh ./deploy.sh app-covpass-demo"
                        }
                        withCredentials([
                            file(credentialsId: 'internal-supply-key', variable: 'SUPPLY_JSON_KEY'),
                        ]) {
                            sh "./run-in-docker.sh ./deploy.sh app-covpass-check-demo"
                        }
                    }
                }
            }
        }
        stage("Firebase") {
            when {
                expression { params.Firebase == 'true' }
            }
            steps {
                withVault(SECRETS) {
                    script {
                        sh 'bundle install --path ".gems"'
                        sh("""
                            bundle exec fastlane deploy_to_firebase
                        """)
                    }
                }
            }
        }
        stage('Archive APKs') {
            steps {
                script {
                    archiveArtifacts 'app-*/**/*.apk'
                }
            }
        }
        stage('Archive Mappings') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'release/*'
                }
            }
            steps {
                script {
                    // Create an empty zip file
                    sh 'echo UEsFBgAAAAAAAAAAAAAAAAAAAAAAAA== | base64 -d > mappings.zip'
                    // Add mappings if they exist
                    sh 'zip -r mappings.zip */build/outputs/mapping/ || true'
                    archiveArtifacts 'mappings.zip'
                }
            }
        }
        stage('Publish Release') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'release/*'
                }
            }
            steps {
                script {
                    // Prevent conflicts for parallel builds
                    sh('git fetch --tags')

                    // Check if we have any tags to push.
                    def toPush = sh(
                        returnStdout: true,
                        script: "git push --dry-run --porcelain --tags | grep '^*' || [ \$? -eq 1 ]"
                    ).trim() != ""

                    // Add extra tag for release branches, so we can track them even when doing fast-forward merges.
                    if (!toPush && env.BRANCH_NAME != "main" && env.BRANCH_NAME != "master") {
                        def prefix = env.BRANCH_NAME.replaceAll(/[^\/a-zA-Z0-9_\-]+/, '-')
                        def version = currentBuild.displayName
                        sh("git tag $prefix-$version || true")
                    }

                    // Only publish release if tag doesn't exist, yet.
                    if (toPush) {
                        gradle('publish', '--stacktrace')
                    }
                }
                finishRelease()
            }
        }
        stage('Publish Souce Labs') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'release/*'
                }
            }
             steps {
                script {
                    withVault(SECRETS) {
                        withCredentials(
                            [usernamePassword(
                                credentialsId: 'jenkins-github-ega', 
                                usernameVariable: 'USERNAME', 
                                passwordVariable: 'PASSWORD')
                            ]
                        ) {
                            def artifactVersion = currentBuild.displayName.replaceAll("v-","") 
                            sh """
                            app_covpass_demo=\$(find . -name covpassdemo*${artifactVersion}*-release.apk)
                            echo \$app_covpass_demo
                            appId=\$(curl -u "${env.SAUCE_USERNAME}:${env.SAUCE_ACCESS_KEY}" --location --request POST 'https://api.eu-central-1.saucelabs.com/v1/storage/upload' --form payload=@"\$app_covpass_demo" --form name="covpassdemo-${artifactVersion}" --form 'description="covpassdemo"|jq -r '.item|.id')
                            if [ -n "\$appId" ]; then
                                git clone https://${USERNAME}:${PASSWORD}@github.ibmgcloud.net/eGA/covpass-test-automation.git
                                cd covpass-test-automation
                                git tag -a ${currentBuild.displayName} -m "appId:\$appId"
                                git push origin --tags
                                cd ..
                                rm -rf covpass-test-automation
                            fi

                            """
                        }
                    }
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
