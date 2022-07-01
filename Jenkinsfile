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
            label "centos7-builder-4c-2g"
        }
    }

    options {
        timestamps()
        timeout(360)
    }

    environment {
        // The settings file needs to exist on the target Jenkins system
        mvnSettings = "aaa-settings"
    }

    stages {
        stage("Java Build") {
            steps {
                lfJava(mvnSettings=env.mvnSettings)
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
