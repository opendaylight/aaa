import org.jenkinsci.plugins.workflow.libs.Library
// SPDX-License-Identifier: Apache-2.0
//
// Copyright (c) 2021 The Linux Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

@Library("lf-pipelines") _

pipeline {
    agent {
        node {
            label "centos7-builder-2c-2g"
        }
    }

    options {
        timestamps()
        timeout(360)
    }

    environment {
        _logSettingsFile = "jenkins-log-archives"
        _mvnSettings = "aaa-settings"
        _javaVersion = "openjdk17"
        _mvnVersion = "mvn38"
        _mvnGlobalSettings = "global-settings"
        WORK_SPACE = "${WORKSPACE}"
        MAVEN_OPTIONS="echo --show-version --batch-mode -Djenkins -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -Dmaven.repo.local=/tmp/r -Dorg.ops4j.pax.url.mvn.localRepository=/tmp/r"
        _mvnGoals ="-e --global-settings ${env.GLOBAL_SETTINGS_FILE} --settings ${env.SETTINGS_FILE} -DaltDeploymentRepository=staging::default::file:${env.WORK_SPACE}/m2repo ${MAVEN_OPTIONS} ${MAVEN_PARAMS}"
    }

    triggers {
        gerrit customUrl: '', gerritProjects: [[branches: [[compareType: 'ANT', pattern: '**']], compareType: 'ANT', disableStrictForbiddenFileVerification: false, pattern: 'aaa']], triggerOnEvents: [patchsetCreated(excludeDrafts: true), commentAddedContains('^Patch Set\\s+\\d+:\\s+(recheck-pipelines)\\s*$')]
    }

    stages {
        stage('Build') {
		    tools {
    			maven 'mvn38'
  			}
            steps {
                sh 'mvn --version'
            }
        }
        //stage('Add Config files') {
        //    steps {
        //        configFileProvider([
        //            configFile(fileId: 'aaa-settings', targetLocation: 'SETTINGS_FILE'),
        //            configFile(fileId: 'global-settings', targetLocation: 'GLOBAL_SETTINGS_FILE')]) {
        //                sh "cat SETTINGS_FILE"
        //                sh "cat GLOBAL_SETTINGS_FILE"
        //        }
        //    }
        //}
        stage("Java Build") {
            steps {
                script {
                    lfJava{
                        mvnSettings = _mvnSettings
                        javaVersion = _javaVersion
                        mvnGlobalSettings = _mvnGlobalSettings
                        mvnGoals = _mvnGoals
                        mvnVersion = _mvnVersion
                    }
                }
            }
        }
    }

    post {
        always {
            script{
                lfInfraShipLogs{logSettingsFile=_logSettingsFile}
            }
        }
    }
}
