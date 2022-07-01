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

loadGlobalLibrary()

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
        mvnSettings = "aaa-settings"
        javaVersion = "openjdk17"
        mvnVersion = "mvn38"
        mvnGlobalSettings = "global-settings"
        WORK_SPACE = "${WORKSPACE}"
        MAVEN_OPTIONS="echo --show-version --batch-mode -Djenkins -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -Dmaven.repo.local=/tmp/r -Dorg.ops4j.pax.url.mvn.localRepository=/tmp/r"
        mvnGoals ="-e --global-settings ${GLOBAL_SETTINGS_FILE} --settings ${env.mvnSettings} -DaltDeploymentRepository=staging::default::file:${env.WORK_SPACE}/m2repo ${MAVEN_OPTIONS} ${MAVEN_PARAMS}"
    }

    triggers {
        gerrit customUrl: '', gerritProjects: [[branches: [[compareType: 'ANT', pattern: '**']], compareType: 'ANT', disableStrictForbiddenFileVerification: false, pattern: 'aaa']], triggerOnEvents: [patchsetCreated(excludeDrafts: true), commentAddedContains('^Patch Set\\s+\\d+:\\s+(recheck-pipelines)\\s*$')]
    }

    stages {
        stage("Java Build") {
            steps {
                lfJava(mvnSettings=env.mvnSettings, javaVersion=env.javaVersion,
                    mvnVersion=env.mvnVersion, mvnGlobalSettings=env.mvnGlobalSettings,
                    mvnGoals=env.mvnGoals)
            }
        }
    }

    post {
        always {
            lfInfraShipLogs()
        }
    }
}

// This loadGlobalLibrary call is only required if the library is not defined
// in the Jenkins global or job settings. Otherwise, a simple "@Library" call
// will suffice.
def loadGlobalLibrary(branch = "*/master") {
    library(identifier: "pipelines@master",
        retriever: legacySCM([
            $class: "GitSCM",
            userRemoteConfigs: [[url: "https://github.com/lfit/releng-pipelines"]],
            branches: [[name: branch]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [[
                $class: "SubmoduleOption",
                recursiveSubmodules: true,
            ]]]
        )
    ) _
}
